package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AdamantWill;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SorinTheMirthless;
import com.github.laxika.magicalvibes.cards.u.UnhallowedPhalanx;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrislyRitual.class, UnhallowedPhalanx.class, SorinTheMirthless.class, Plains.class,
        AdamantWill.class})
class GrislyRitualTest extends BaseCardTest {

    @Test
    void destroysCreatureAndCreatesTwoBloodTokens() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new UnhallowedPhalanx());

        cast(creature.getId());

        harness.assertInGraveyard(player2, "Unhallowed Phalanx");
        harness.assertNotOnBattlefield(player2, "Unhallowed Phalanx");
        assertThat(countPermanents(player1, "Blood")).isEqualTo(2);
        assertThat(countPermanents(player2, "Blood")).isZero();
    }

    @Test
    void destroysPlaneswalkerAndCreatesTwoBloodTokens() {
        Permanent sorin = harness.addToBattlefieldAndReturn(player2, new SorinTheMirthless());
        sorin.setCounterCount(CounterType.LOYALTY, 4);

        cast(sorin.getId());

        harness.assertInGraveyard(player2, "Sorin the Mirthless");
        harness.assertNotOnBattlefield(player2, "Sorin the Mirthless");
        assertThat(countPermanents(player1, "Blood")).isEqualTo(2);
    }

    @Test
    void cannotTargetNonCreatureNonPlaneswalker() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new GrislyRitual()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(harness.getPermanentId(player2, "Plains"))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new GrislyRitual()));
        addMana();
        harness.castSorcery(player1, 0, List.of(targetId));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    @Test
    void canDestroyOwnCreatureAndCreateBlood() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new UnhallowedPhalanx());

        cast(creature.getId());

        harness.assertInGraveyard(player1, "Unhallowed Phalanx");
        harness.assertNotOnBattlefield(player1, "Unhallowed Phalanx");
        assertThat(countPermanents(player1, "Blood")).isEqualTo(2);
        assertThat(countPermanents(player2, "Blood")).isZero();
    }

    @Test
    void createsNoBloodWhenTargetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new UnhallowedPhalanx());
        harness.setHand(player1, List.of(new GrislyRitual()));
        addMana();
        harness.castSorcery(player1, 0, List.of(creature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Blood")).isZero();
        assertThat(countPermanents(player2, "Blood")).isZero();
        harness.assertInGraveyard(player1, "Grisly Ritual");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void createsBloodEvenWhenIndestructiblePreventsDestruction() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new UnhallowedPhalanx());
        harness.setHand(player1, List.of(new GrislyRitual()));
        addMana();
        harness.castSorcery(player1, 0, List.of(creature.getId()));
        harness.setHand(player2, List.of(new AdamantWill()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Unhallowed Phalanx");
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(creature.getCard());
        assertThat(countPermanents(player1, "Blood")).isEqualTo(2);
        assertThat(countPermanents(player2, "Blood")).isZero();
    }

    @Test
    void bloodTokenDiscardsAndSacrificesAsCostsThenDrawsOnResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new UnhallowedPhalanx());
        cast(creature.getId());
        Permanent blood = findPermanent(player1, "Blood");
        Plains discarded = new Plains();
        Plains drawn = new Plains();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(blood), null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blood);
        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }
}
