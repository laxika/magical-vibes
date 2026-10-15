package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JadecraftArtisan.class, GrizzlyBears.class})
class JadecraftArtisanTest extends BaseCardTest {

    @Test
    void etbBoostsTargetCreatureUntilEndOfTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new JadecraftArtisan()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new JadecraftArtisan()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void canTargetItselfWhenNoOtherCreatureIsPresent() {
        harness.setHand(player1, List.of(new JadecraftArtisan()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Jadecraft Artisan");
        Permanent artisan = findPermanent(player1, "Jadecraft Artisan");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, artisan.getId());
        harness.passBothPriorities();

        assertThat(artisan.getEffectivePower()).isEqualTo(5);
        assertThat(artisan.getEffectiveToughness()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringWithoutBeingCastStillBoostsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JadecraftArtisan());
        harness.enterBattlefieldAndReturn(player1, new JadecraftArtisan());
        resolveAllTriggers();

        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    void abilityStillResolvesAfterSourceLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JadecraftArtisan());
        harness.setHand(player1, List.of(new JadecraftArtisan()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, "Jadecraft Artisan");
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
    }
}
