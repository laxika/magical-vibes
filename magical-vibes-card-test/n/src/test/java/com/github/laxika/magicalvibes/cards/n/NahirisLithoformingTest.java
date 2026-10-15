package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AshayaSoulOfTheWild;
import com.github.laxika.magicalvibes.cards.c.ClericOfChillDepths;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KorCelebrant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.TaboraxHopesDemise;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NahirisLithoforming.class, Forest.class, Island.class, Mountain.class, ClericOfChillDepths.class,
        AshayaSoulOfTheWild.class, KorCelebrant.class, TaboraxHopesDemise.class})
class NahirisLithoformingTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices X lands, draws for the actual number, and grants X land plays")
    void resolvesAllEffectsFromTheAnnouncedX() {
        List<Permanent> lands = List.of(
                harness.addToBattlefieldAndReturn(player1, new Mountain()),
                harness.addToBattlefieldAndReturn(player1, new Mountain()),
                harness.addToBattlefieldAndReturn(player1, new Mountain()));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ClericOfChillDepths());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.setHand(player1, List.of(new NahirisLithoforming()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 2);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validIds()).containsExactlyInAnyOrderElementsOf(
                lands.stream().map(Permanent::getId).toList());

        harness.handleMultiplePermanentsChosen(player1, List.of(lands.get(0).getId(), lands.get(1).getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, lands.get(2));
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lands.get(0), lands.get(1));
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Island");
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(3);

        Permanent ownLand = harness.enterBattlefieldAndReturn(player1, new Mountain());
        Permanent opposingLand = harness.enterBattlefieldAndReturn(player2, new Mountain());
        assertThat(ownLand.isTapped()).isTrue();
        assertThat(opposingLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("When X exceeds the land count, draws only for lands actually sacrificed")
    void fewerLandsThanX() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.setHand(player1, List.of(new NahirisLithoforming()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(4);
    }

    @Test
    @DisplayName("With no lands, draws nothing but still grants X additional land plays")
    void noLandsStillGrantsAdditionalPlays() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new NahirisLithoforming(), new Mountain(), new Island(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.playLand(player1, 0);
        harness.playLand(player1, 0);
        harness.playLand(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("X zero sacrifices and draws nothing but still makes later lands enter tapped")
    void zeroXStillAppliesTappedEntry() {
        Permanent existingLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new NahirisLithoforming()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(existingLand);
        assertThat(existingLand.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(1);
        assertThat(harness.enterBattlefieldAndReturn(player1, new Forest()).isTapped()).isTrue();
        assertThat(harness.enterBattlefieldAndReturn(player1, new ClericOfChillDepths()).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Additional land permissions accumulate and both turn effects expire")
    void cumulativePermissionsExpireAfterThisTurn() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island()));
        harness.setHand(player1, List.of(new NahirisLithoforming(), new NahirisLithoforming()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 1);
        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(4);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(1);
        assertThat(harness.enterBattlefieldAndReturn(player1, new Mountain()).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Sacrificing all creature lands simultaneously preserves their death triggers")
    void allCreatureLandsAreSacrificedSimultaneously() {
        harness.addToBattlefield(player1, new TaboraxHopesDemise());
        harness.addToBattlefield(player1, new KorCelebrant());
        harness.addToBattlefield(player1, new AshayaSoulOfTheWild());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain(), new Forest()));
        harness.setHand(player1, List.of(new NahirisLithoforming()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.stack).hasSize(2);
    }
}
