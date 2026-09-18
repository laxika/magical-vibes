package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Concentrate;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.cards.g.GhastlyDemise;
import com.github.laxika.magicalvibes.cards.n.NomadDecoy;
import com.github.laxika.magicalvibes.cards.s.ShowerOfCoals;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Divert.class, Concentrate.class, Firebolt.class, ShowerOfCoals.class,
        GhastlyDemise.class, NomadDecoy.class})
class DivertTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Divert requires a single-target spell")
    void castingRequiresSingleTargetSpell() {
        Concentrate concentrate = new Concentrate();
        harness.setHand(player1, List.of(concentrate));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Divert()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, concentrate.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single target");
    }

    @Test
    @DisplayName("Casting Divert rejects a spell with multiple targets")
    void castingRejectsMultiTargetSpell() {
        ShowerOfCoals showerOfCoals = new ShowerOfCoals();
        harness.setHand(player1, List.of(showerOfCoals));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, List.of(player1.getId(), player2.getId()));
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Divert()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, showerOfCoals.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single target");
    }

    @Test
    @DisplayName("The spell's controller can pay to keep the original target")
    void payingKeepsOriginalTarget() {
        Firebolt firebolt = new Firebolt();
        harness.setHand(player1, List.of(firebolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new Divert()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, firebolt.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Declining to pay lets Divert choose a new legal target")
    void decliningRetargetsSpell() {
        Firebolt firebolt = new Firebolt();
        harness.setHand(player1, List.of(firebolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new Divert()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, firebolt.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId())
                .doesNotContain(player2.getId());

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("If the spell's controller cannot pay, Divert retargets without a pay choice")
    void cannotPayRetargetsImmediately() {
        Firebolt firebolt = new Firebolt();
        harness.setHand(player1, List.of(firebolt));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new Divert()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, firebolt.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Declining to pay leaves the spell unchanged when no alternative target exists")
    void noAlternativeTargetLeavesSpellUnchanged() {
        Permanent onlyCreature = addCreatureReady(player1, new NomadDecoy());
        GhastlyDemise ghastlyDemise = new GhastlyDemise();
        harness.setHand(player1, List.of(ghastlyDemise));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, onlyCreature.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Divert()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, ghastlyDemise.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(onlyCreature.getId());
    }

    @Test
    @DisplayName("Casting Divert cannot target a single-target ability")
    void castingRejectsSingleTargetAbility() {
        NomadDecoy decoy = new NomadDecoy();
        Permanent source = addCreatureReady(player1, decoy);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, source.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Divert()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, decoy.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell");
    }
}
