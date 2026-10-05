package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.a.AleshasVanguard;
import com.github.laxika.magicalvibes.cards.s.SoulSummons;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QarsiHighPriest.class, AleshasVanguard.class, SoulSummons.class})
class QarsiHighPriestTest extends BaseCardTest {

    @Test
    @DisplayName("Pays mana, taps, sacrifices another creature, and manifests the top card")
    void activatesAndManifestsTopCard() {
        Permanent priest = addCreatureReady(player1, new QarsiHighPriest());
        Permanent fodder = addCreatureReady(player1, new AleshasVanguard());
        harness.setLibrary(player1, List.of(new AleshasVanguard()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown)
                .findFirst()
                .orElseThrow();
        assertThat(priest.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(priest).doesNotContain(fodder);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(fodder.getCard());
        assertThat(manifested.isManifested()).isTrue();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without another creature to sacrifice")
    void cannotActivateWithoutAnotherCreature() {
        addCreatureReady(player1, new QarsiHighPriest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature to sacrifice");
    }

    @Test
    void paysCostsBeforeResolutionEvenWithEmptyLibrary() {
        Permanent priest = addCreatureReady(player1, new QarsiHighPriest());
        Permanent fodder = addCreatureReady(player1, new AleshasVanguard());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(priest.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(priest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(fodder.getCard());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(priest);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void manifestedCreatureTurnsFaceUpForItsManaCost() {
        addCreatureReady(player1, new QarsiHighPriest());
        addCreatureReady(player1, new AleshasVanguard());
        AleshasVanguard topCard = new AleshasVanguard();
        SoulSummons nextCard = new SoulSummons();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent manifested = findPermanent(player1, "Alesha's Vanguard");
        assertThat(manifested.getCard()).isSameAs(topCard);
        assertThat(manifested.isManifested()).isTrue();
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested));

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(manifested.isManifested()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void manifestedNoncreatureCannotTurnFaceUpForItsManaCost() {
        addCreatureReady(player1, new QarsiHighPriest());
        addCreatureReady(player1, new AleshasVanguard());
        harness.setLibrary(player1, List.of(new SoulSummons()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent manifested = findPermanent(player1, "Soul Summons");
        assertThat(manifested.isManifested()).isTrue();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(manifested)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(manifested.isFaceDown()).isTrue();
    }

    @Test
    void abilityResolvesAfterPriestLeavesBattlefield() {
        Permanent priest = addCreatureReady(player1, new QarsiHighPriest());
        addCreatureReady(player1, new AleshasVanguard());
        harness.setLibrary(player1, List.of(new SoulSummons()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(priest);
        gd.playerGraveyards.get(player1.getId()).add(priest.getCard());
        harness.passBothPriorities();

        Permanent manifested = findPermanent(player1, "Soul Summons");
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(manifested.isManifested()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent priest = harness.addToBattlefieldAndReturn(player1, new QarsiHighPriest());
        Permanent fodder = addCreatureReady(player1, new AleshasVanguard());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(priest.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(priest, fodder);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSacrificeOpponentsCreature() {
        Permanent priest = addCreatureReady(player1, new QarsiHighPriest());
        Permanent opponentCreature = addCreatureReady(player2, new AleshasVanguard());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature to sacrifice");
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(priest);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponentCreature);
        assertThat(gd.stack).isEmpty();
    }
}
