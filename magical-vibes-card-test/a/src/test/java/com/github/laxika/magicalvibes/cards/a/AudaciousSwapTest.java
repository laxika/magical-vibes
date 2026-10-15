package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LifesLegacy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AudaciousSwap.class, AwakeningZone.class, Forest.class, GloriousAnthem.class,
        GrizzlyBears.class, LifesLegacy.class})
class AudaciousSwapTest extends BaseCardTest {

    @Test
    @DisplayName("Shuffles the target and puts an exiled top-card land onto the battlefield")
    void shufflesTargetAndPutsLandOntoBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new AudaciousSwap()));
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Forest");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.landsPlayedThisTurn.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Offers an exiled nonland top card for a free cast")
    void offersNonlandForFreeCast() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new AudaciousSwap()));
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casualty copies Audacious Swap and allows a new target")
    void casualtyCopiesAndRetargets() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent casualty = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new AudaciousSwap()));
        addMana();

        harness.castInstantWithSacrifice(player1, 0, firstTarget.getId(), casualty.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Forest", "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(casualty.getId()));
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new AudaciousSwap()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonenchantment permanent");
    }

    @Test
    @DisplayName("Declining the free cast leaves the nonland card in exile")
    void decliningFreeCastLeavesCardExiled() {
        GrizzlyBears card = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player2, card);
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new AudaciousSwap()));
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(card);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A stolen permanent uses its owner's library and returns under its owner's control")
    void stolenPermanentUsesOwnersLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        gd.stolenCreatures.put(target.getId(), player2.getId());
        Forest controllersTopCard = new Forest();
        harness.setLibrary(player1, List.of(controllersTopCard));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new AudaciousSwap()));
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getCard).containsExactly(target.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(controllersTopCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An absent target prevents the shuffle and exile instructions")
    void absentTargetDoesNotExileTopCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new AudaciousSwap()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A free spell with an unpayable additional sacrifice cost stays exiled")
    void cannotCastWithoutPayingMandatoryAdditionalCost() {
        harness.addToBattlefield(player2, new AwakeningZone());
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        Permanent spawn = findPermanents(player2, "Eldrazi Spawn").getFirst();
        LifesLegacy card = new LifesLegacy();
        harness.setLibrary(player2, List.of(card));
        harness.setHand(player1, List.of(new AudaciousSwap()));
        addMana();

        harness.castAndResolveInstant(player1, 0, spawn.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(card);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(spawn.getId()));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
