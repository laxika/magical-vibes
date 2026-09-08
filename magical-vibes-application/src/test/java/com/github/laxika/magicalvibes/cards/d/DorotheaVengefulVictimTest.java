package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DorotheaVengefulVictim.class, DorotheasRetribution.class, FountainOfYouth.class, GrizzlyBears.class})
class DorotheaVengefulVictimTest extends BaseCardTest {

    @Test
    @DisplayName("Dorothea is sacrificed at end of combat after attacking")
    void frontFaceIsSacrificedAfterAttacking() {
        Permanent dorothea = addCreatureReady(player1, new DorotheaVengefulVictim());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dorothea);
    }

    @Test
    @DisplayName("Dorothea is sacrificed at end of combat after blocking")
    void frontFaceIsSacrificedAfterBlocking() {
        Permanent dorothea = addCreatureReady(player1, new DorotheaVengefulVictim());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dorothea);
    }

    @Test
    @DisplayName("Disturb casts Dorothea's Retribution transformed and grants its attack trigger")
    void disturbCreatesTappedAttackingSpiritThatIsSacrificedAtEndOfCombat() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = castWithDisturb(creature);

        assertThat(aura.isTransformed()).isTrue();
        assertThat(aura.getCard()).isInstanceOf(DorotheasRetribution.class);
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(entry -> entry.plainText()).toList())
                .contains("A 4/4 Spirit creature token enters the battlefield tapped and attacking.");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Spirit"))).isTrue();
    }

    @Test
    @DisplayName("Disturb requires a creature target")
    void disturbRequiresCreatureTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new DorotheaVengefulVictim()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Dorothea's Retribution is exiled instead of going to the graveyard")
    void transformedAuraIsExiledInsteadOfGoingToGraveyard() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = castWithDisturb(creature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, aura));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .contains(aura.getOriginalCard().getId());
    }

    private Permanent castWithDisturb(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new DorotheaVengefulVictim()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof DorotheaVengefulVictim)
                .findFirst()
                .orElseThrow();
    }
}
