package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntroductionToAnnihilation;
import com.github.laxika.magicalvibes.cards.r.Resculpt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Snakeform;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElementalExpressionist.class, BarkshellBlessing.class, GrizzlyBears.class, Shock.class,
        Unsummon.class, IntroductionToAnnihilation.class, Resculpt.class, Snakeform.class})
class ElementalExpressionistTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant grants the target exile replacement and creates an Elemental on bounce")
    void castingInstantGrantsExileReplacementAndCreatesToken() {
        Permanent target = setUpAndResolveMagecraft();

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Grizzly Bears"));
        assertThat(findPermanents(player1, "Elemental")).hasSize(1);
    }

    @Test
    @DisplayName("Casting and copying an instant grants two independent magecraft instances")
    void copyingInstantCreatesTwoTokensWhenTargetLeaves() {
        harness.addToBattlefield(player1, new ElementalExpressionist());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handlePermanentChosen(player1, target.getId());
        for (int i = 0; i < 6 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elemental")).hasSize(2);
    }

    @Test
    @DisplayName("The granted replacement and trigger expire at end of turn")
    void grantedAbilitiesExpireAtEndOfTurn() {
        Permanent target = setUpAndResolveMagecraft();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Elemental")).isEmpty();
    }

    @Test
    @DisplayName("Magecraft cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new ElementalExpressionist());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Lethal damage exiles the creature and creates an Elemental instead of a death")
    void lethalDamageCreatesToken() {
        Permanent target = setUpAndResolveMagecraft();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Grizzly Bears"));
        assertThat(findPermanents(player1, "Elemental")).hasSize(1);
    }

    @Test
    @DisplayName("A sorcery cast grants the abilities and they survive Expressionist leaving")
    void sorceryGrantsAbilitiesEvenWhenExpressionistIsExiled() {
        harness.addToBattlefield(player1, new ElementalExpressionist());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new IntroductionToAnnihilation()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, findPermanent(player1, "Elemental Expressionist").getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Elemental Expressionist");
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Elemental")).hasSize(1);
    }

    @Test
    @DisplayName("Direct exile triggers the granted ability in addition to Resculpt's token")
    void directExileCreatesAdditionalToken() {
        Permanent target = setUpAndResolveMagecraft();

        harness.setHand(player2, List.of(new Resculpt()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elemental")).hasSize(2);
    }

    @Test
    @DisplayName("A token targeted by magecraft creates a replacement token when bounced")
    void exiledTokenCreatesAnotherToken() {
        harness.addToBattlefield(player1, new ElementalExpressionist());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Resculpt()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        Permanent token = findPermanent(player1, "Elemental");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, token.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, token.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elemental")).hasSize(1);
        assertThat(findPermanent(player1, "Elemental").getId()).isNotEqualTo(token.getId());
    }

    @Test
    @DisplayName("Losing all abilities after magecraft removes the exile replacement")
    void losingAbilitiesRemovesExileReplacement() {
        Permanent target = setUpAndResolveMagecraft();
        removeGrantedAbilities(target);

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Elemental")).isEmpty();
    }

    @Test
    @DisplayName("Losing all abilities after magecraft removes the token trigger on direct exile")
    void losingAbilitiesRemovesExileTrigger() {
        Permanent target = setUpAndResolveMagecraft();
        removeGrantedAbilities(target);

        harness.setHand(player2, List.of(new Resculpt()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elemental")).hasSize(1);
    }

    private void removeGrantedAbilities(Permanent target) {
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new Snakeform()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player2, 0, target.getId());
    }

    private Permanent setUpAndResolveMagecraft() {
        harness.addToBattlefield(player1, new ElementalExpressionist());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return target;
    }
}
