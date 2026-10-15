package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.d.DigThroughTime;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TimeWarp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeitmotifComposer.class, Divination.class, GrizzlyBears.class, TimeWarp.class,
        Fireball.class, AirElemental.class, DigThroughTime.class})
class LeitmotifComposerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player draws a card")
    void combatDamageDrawsCard() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Divination()));
        harness.setLife(player2, 20);
        addReadyComposer(player1);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting an instant or sorcery with mana value 5 or greater creates a copy")
    void highManaValueInstantOrSorceryCreatesCopy() {
        addReadyComposer(player1);
        harness.setHand(player1, List.of(new TimeWarp()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Casting a spell below mana value 5 does not create a copy")
    void lowManaValueInstantOrSorceryDoesNotCreateCopy() {
        addReadyComposer(player1);
        harness.setHand(player1, List.of(new Divination()));
        harness.setLibrary(player1, List.of(new Divination(), new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The activated ability affects every creature named Leitmotif Composer")
    void activatedAbilityAffectsMatchingCreaturesOnly() {
        Permanent source = addReadyComposer(player1);
        Permanent otherComposer = addReadyComposer(player1);
        Permanent opponentComposer = addReadyComposer(player2);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, source)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, otherComposer)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, opponentComposer)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, bears)).isFalse();
    }

    @Test
    @DisplayName("An instant paid for with delve still uses its full mana value")
    void highManaValueInstantWithDelveCreatesCopy() {
        addReadyComposer(player1);
        harness.setHand(player1, List.of(new DigThroughTime()));
        harness.setGraveyard(player1, List.of(new LeitmotifComposer(), new LeitmotifComposer(),
                new LeitmotifComposer(), new LeitmotifComposer(), new LeitmotifComposer(),
                new LeitmotifComposer()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, null, List.of(0, 1, 2, 3, 4, 5));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An opponent casting a qualifying spell does not trigger the Composer")
    void opponentSpellDoesNotCreateCopy() {
        addReadyComposer(player2);
        harness.setHand(player1, List.of(new TimeWarp()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("X counts toward the mana value of a spell on the stack")
    void xSpellAtThresholdCreatesCopy() {
        addReadyComposer(player1);
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 4, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("An X spell with mana value four does not create a copy")
    void xSpellBelowThresholdDoesNotCreateCopy() {
        addReadyComposer(player1);
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 3, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A creature spell with mana value five does not create a copy")
    void highManaValueCreatureDoesNotCreateCopy() {
        addReadyComposer(player1);
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Leitmotif Composer")).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Token copies retain the spell cast trigger")
    void tokenCopiesAlsoCreateCopiesOnLaterCasts() {
        addReadyComposer(player1);
        harness.setHand(player1, List.of(new TimeWarp(), new TimeWarp()));
        harness.addMana(player1, ManaColor.BLUE, 10);

        harness.castSorcery(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);

        harness.castSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("The blocking restriction also applies to Composers entering later that turn")
    void activatedAbilityAffectsLaterEntrants() {
        addReadyComposer(player1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent laterComposer = harness.addToBattlefieldAndReturn(player2, new LeitmotifComposer());

        assertThat(gqs.hasCantBeBlocked(gd, laterComposer)).isTrue();
    }

    private Permanent addReadyComposer(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new LeitmotifComposer());
    }
}
