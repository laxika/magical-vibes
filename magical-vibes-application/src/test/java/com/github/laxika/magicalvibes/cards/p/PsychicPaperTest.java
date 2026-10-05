package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PsychicPaper.class, GrizzlyBears.class, HillGiant.class, GiantGrowth.class})
class PsychicPaperTest extends BaseCardTest {

    @Test
    void choosesCreatureNameAndTypeAndGrantsEvasionAndWard() {
        Permanent paper = harness.addToBattlefieldAndReturn(player1, new PsychicPaper());
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new HillGiant());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Hill Giant");
        harness.handleListChoice(player1, "WIZARD");

        assertThat(gqs.getEffectiveName(gd, host)).isEqualTo("Hill Giant");
        assertThat(gqs.effectiveCreatureSubtypes(gd, host)).containsExactly(CardSubtype.WIZARD);
        assertThat(gqs.hasKeyword(gd, host, Keyword.WARD)).isTrue();

        declareAttackersAndPrepareBlockers(List.of(1));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");

        assertThat(paper.getAttachedTo()).isEqualTo(host.getId());
    }

    @Test
    void choicesAreMadeDuringEquipResolutionWithoutAnAttachmentTrigger() {
        harness.addToBattlefield(player1, new PsychicPaper());
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Hill Giant");
        harness.handleListChoice(player1, "WIZARD");

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectiveName(gd, host)).isEqualTo("Hill Giant");
        assertThat(gqs.effectiveCreatureSubtypes(gd, host)).containsExactly(CardSubtype.WIZARD);
    }

    @Test
    void movingPaperRestoresOldCreatureAndMakesNewChoicesForNewCreature() {
        harness.addToBattlefield(player1, new PsychicPaper());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new HillGiant());
        equipAndChoose(first, "Hill Giant", "WIZARD");

        equipAndChoose(second, "Grizzly Bears", "BEAR");

        assertThat(gqs.getEffectiveName(gd, first)).isEqualTo("Grizzly Bears");
        assertThat(gqs.effectiveCreatureSubtypes(gd, first)).containsExactly(CardSubtype.BEAR);
        assertThat(gqs.hasKeyword(gd, first, Keyword.WARD)).isFalse();
        assertThat(gqs.getEffectiveName(gd, second)).isEqualTo("Grizzly Bears");
        assertThat(gqs.effectiveCreatureSubtypes(gd, second)).containsExactly(CardSubtype.BEAR);
        assertThat(gqs.hasKeyword(gd, second, Keyword.WARD)).isTrue();
    }

    @Test
    void equippingSameCreatureDoesNotChooseAgain() {
        harness.addToBattlefield(player1, new PsychicPaper());
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        equipAndChoose(host, "Hill Giant", "WIZARD");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectiveName(gd, host)).isEqualTo("Hill Giant");
        assertThat(gqs.effectiveCreatureSubtypes(gd, host)).containsExactly(CardSubtype.WIZARD);
    }

    @Test
    void cannotChooseNoncreatureCardNameOrNoncreatureSubtype() {
        harness.addToBattlefield(player1, new PsychicPaper());
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Psychic Paper"))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, "Hill Giant");
        assertThatThrownBy(() -> harness.handleListChoice(player1, "EQUIPMENT"))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, "WIZARD");

        assertThat(gqs.effectiveCreatureSubtypes(gd, host)).containsExactly(CardSubtype.WIZARD);
    }

    @Test
    void wardCountersOpponentSpellWithoutPayment() {
        harness.addToBattlefield(player1, new PsychicPaper());
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        equipAndChoose(host, "Hill Giant", "WIZARD");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, host.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Giant Growth");
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
    }

    @Test
    void payingWardAllowsOpponentSpellToResolve() {
        harness.addToBattlefield(player1, new PsychicPaper());
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        equipAndChoose(host, "Hill Giant", "WIZARD");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castInstant(player2, 0, host.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(5);
    }

    @Test
    void wardDoesNotTaxControllersOwnSpell() {
        harness.addToBattlefield(player1, new PsychicPaper());
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        equipAndChoose(host, "Hill Giant", "WIZARD");
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, host.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(5);
    }

    private void equipAndChoose(Permanent host, String name, String type) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();
        if (!gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }
        harness.handleListChoice(player1, name);
        harness.handleListChoice(player1, type);
    }
}
