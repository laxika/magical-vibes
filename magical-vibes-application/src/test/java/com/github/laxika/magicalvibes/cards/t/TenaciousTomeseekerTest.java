package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HopefulVigil;
import com.github.laxika.magicalvibes.cards.i.IntoTheFaeCourt;
import com.github.laxika.magicalvibes.cards.k.KindledHeroism;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TenaciousTomeseeker.class, DarksteelRelic.class, GrizzlyBears.class, Shock.class,
        HopefulVigil.class, IntoTheFaeCourt.class, KindledHeroism.class, PropheticPrism.class})
class TenaciousTomeseekerTest extends BaseCardTest {

    @Test
    void withoutBargainDoesNotReturnCard() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new TenaciousTomeseeker()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void withBargainReturnsTargetInstantOrSorcery() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.setHand(player1, List.of(new TenaciousTomeseeker()));
        addMana();

        harness.castKickedCreatureWithPermanent(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
        harness.assertNotInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Darksteel Relic");
    }

    @Test
    void bargainCannotTargetCreatureCard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.setHand(player1, List.of(new TenaciousTomeseeker()));
        addMana();

        harness.castKickedCreatureWithPermanent(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void bargainReturnsSorceryAndLeavesOtherCardsInGraveyard() {
        IntoTheFaeCourt sorcery = new IntoTheFaeCourt();
        harness.setGraveyard(player1, List.of(sorcery, new KindledHeroism(), new TenaciousTomeseeker()));
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        harness.setHand(player1, List.of(new TenaciousTomeseeker()));
        addMana();

        harness.castKickedCreatureWithPermanent(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Into the Fae Court");
        harness.assertInGraveyard(player1, "Kindled Heroism");
        harness.assertInGraveyard(player1, "Tenacious Tomeseeker");
        harness.assertInGraveyard(player1, "Prophetic Prism");
    }

    @Test
    void bargainCanSacrificeEnchantment() {
        KindledHeroism instant = new KindledHeroism();
        harness.setGraveyard(player1, List.of(instant));
        harness.setLibrary(player1, List.of());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new HopefulVigil());
        harness.setHand(player1, List.of(new TenaciousTomeseeker()));
        addMana();

        harness.castKickedCreatureWithPermanent(player1, 0, sacrifice.getId());
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Hopeful Vigil");
        harness.assertInHand(player1, "Kindled Heroism");
    }

    @Test
    void bargainCanSacrificeCreatureToken() {
        harness.enterBattlefieldAndReturn(player1, new HopefulVigil());
        resolveAllTriggers();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        KindledHeroism instant = new KindledHeroism();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new TenaciousTomeseeker()));
        addMana();

        harness.castKickedCreatureWithPermanent(player1, 0, token.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(token.getId()));
        harness.assertOnBattlefield(player1, "Hopeful Vigil");
        harness.assertInHand(player1, "Kindled Heroism");
    }

    @Test
    void bargainCannotSacrificeNontokenCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TenaciousTomeseeker());
        harness.setHand(player1, List.of(new TenaciousTomeseeker()));
        addMana();

        assertThatThrownBy(() -> harness.castKickedCreatureWithPermanent(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Tenacious Tomeseeker");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void bargainDoesNotReturnOpponentsGraveyardCard() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new KindledHeroism()));
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        harness.setHand(player1, List.of(new TenaciousTomeseeker()));
        addMana();

        harness.castKickedCreatureWithPermanent(player1, 0, sacrifice.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Kindled Heroism");
        harness.assertNotInHand(player1, "Kindled Heroism");
    }

    @Test
    void removedTargetDoesNotReturnAnotherCardInstead() {
        KindledHeroism target = new KindledHeroism();
        IntoTheFaeCourt other = new IntoTheFaeCourt();
        harness.setGraveyard(player1, List.of(target, other));
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        harness.setHand(player1, List.of(new TenaciousTomeseeker()));
        addMana();

        harness.castKickedCreatureWithPermanent(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other, sacrifice.getCard()));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Kindled Heroism");
        harness.assertInGraveyard(player1, "Into the Fae Court");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringWithoutBeingCastDoesNotTriggerRecovery() {
        harness.setGraveyard(player1, List.of(new KindledHeroism()));

        harness.enterBattlefieldAndReturn(player1, new TenaciousTomeseeker());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Kindled Heroism");
        harness.assertNotInHand(player1, "Kindled Heroism");
    }

    @Test
    void bargainCannotSacrificeOpponentsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        harness.setHand(player1, List.of(new TenaciousTomeseeker()));
        addMana();

        assertThatThrownBy(() -> harness.castKickedCreatureWithPermanent(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Tenacious Tomeseeker");
        harness.assertOnBattlefield(player2, "Prophetic Prism");
        assertThat(gd.stack).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
