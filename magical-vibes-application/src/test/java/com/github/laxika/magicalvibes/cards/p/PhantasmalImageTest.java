package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AetherAdept;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GideonsLawkeeper;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhantasmalImage.class, GrizzlyBears.class, GiantGrowth.class, GideonsLawkeeper.class, AetherAdept.class})
class PhantasmalImageTest extends BaseCardTest {

    private Permanent copyGrizzlyBears() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PhantasmalImage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        return findPermanent(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Phantasmal Image enters as a copy of a creature and is an Illusion in addition to its other types")
    void copiesCreatureAndIsIllusion() {
        Permanent image = copyGrizzlyBears();

        assertThat(image).isNotNull();
        assertThat(image.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(image.getCard().getPower()).isEqualTo(2);
        assertThat(image.getCard().getToughness()).isEqualTo(2);
        assertThat(image.getCard().getSubtypes())
                .contains(CardSubtype.BEAR, CardSubtype.ILLUSION);
    }

    @Test
    @DisplayName("The copy is sacrificed when it becomes the target of a spell")
    void sacrificedWhenTargetedBySpell() {
        Permanent image = copyGrizzlyBears();
        assertThat(image).isNotNull();

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, image.getId());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getOriginalCard().getName().equals("Phantasmal Image"));
        harness.assertInGraveyard(player1, "Phantasmal Image");
        // The copied original is untouched
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Phantasmal Image enters as a 0/0 and dies when the controller declines to copy")
    void diesWhenPlayerDeclines() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PhantasmalImage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getOriginalCard().getName().equals("Phantasmal Image"));
        harness.assertInGraveyard(player1, "Phantasmal Image");
    }

    private Permanent enterCopyOf(UUID creatureId) {
        harness.setHand(player1, List.of(new PhantasmalImage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creatureId);
        return gd.playerBattlefields.get(player1.getId()).getLast();
    }

    @Test
    void sacrificedWhenTargetedByOpponentsActivatedAbility() {
        Permanent lawkeeper = addCreatureReady(player2, new GideonsLawkeeper());
        Permanent image = enterCopyOf(lawkeeper.getId());
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.activateAbility(player2, 0, null, image.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(image);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(image);
        harness.assertInGraveyard(player1, "Phantasmal Image");
        assertThat(lawkeeper.isTapped()).isTrue();
    }

    @Test
    void copiedActivatedAbilityWorks() {
        Permanent lawkeeper = addCreatureReady(player2, new GideonsLawkeeper());
        Permanent image = enterCopyOf(lawkeeper.getId());
        image.setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, lawkeeper.getId());
        harness.passBothPriorities();

        assertThat(lawkeeper.isTapped()).isTrue();
        assertThat(image.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(image);
    }

    @Test
    void copyingAnotherImageDoesNotTargetOrSacrificeIt() {
        Permanent lawkeeper = addCreatureReady(player2, new GideonsLawkeeper());
        Permanent first = enterCopyOf(lawkeeper.getId());
        Permanent second = enterCopyOf(first.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first, second);
        assertThat(gd.stack).isEmpty();
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.activateAbility(player2, 0, null, second.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first).doesNotContain(second);
        harness.assertInGraveyard(player1, "Phantasmal Image");
    }

    @Test
    void copiesFaceDownCharacteristicsInsteadOfUnderlyingCard() {
        Permanent faceDown = harness.addToBattlefieldAndReturn(player2, new GideonsLawkeeper());
        faceDown.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        Permanent image = enterCopyOf(faceDown.getId());

        assertThat(image.isFaceDown()).isFalse();
        assertThat(image.getEffectivePower()).isEqualTo(2);
        assertThat(image.getEffectiveToughness()).isEqualTo(2);
        assertThat(image.getCard().getSubtypes()).containsExactly(CardSubtype.ILLUSION);
    }

    @Test
    void copiedEntersAbilityTriggers() {
        Permanent adept = harness.addToBattlefieldAndReturn(player2, new AetherAdept());
        Permanent image = enterCopyOf(adept.getId());
        harness.handlePermanentChosen(player1, adept.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Aether Adept");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(adept);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(image);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void diesWithoutCopyWhenBattlefieldHasNoCreatures() {
        harness.setHand(player1, List.of(new PhantasmalImage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Phantasmal Image");
        harness.assertNotOnBattlefield(player1, "Phantasmal Image");
    }
}
