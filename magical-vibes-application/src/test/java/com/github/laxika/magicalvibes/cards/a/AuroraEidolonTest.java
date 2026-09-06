package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AuroraEidolon.class, AdelizTheCinderWind.class, GrizzlyBears.class, LightningBolt.class})
class AuroraEidolonTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Aurora Eidolon prevents the next 3 damage to a player")
    void sacrificeAbilityPreventsDamageToPlayer() {
        harness.addToBattlefield(player1, new AuroraEidolon());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aurora Eidolon");
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(3);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The prevention ability can target a creature")
    void preventionAbilityTargetsCreature() {
        harness.addToBattlefield(player1, new AuroraEidolon());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Casting a multicolored spell may return Aurora Eidolon from the graveyard")
    void multicoloredSpellReturnsEidolonToHand() {
        AuroraEidolon eidolon = new AuroraEidolon();
        harness.setGraveyard(player1, List.of(eidolon));
        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(eidolon);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(eidolon);
    }

    @Test
    @DisplayName("Declining the multicolored spell trigger keeps Aurora Eidolon in the graveyard")
    void decliningReturnKeepsEidolonInGraveyard() {
        AuroraEidolon eidolon = new AuroraEidolon();
        harness.setGraveyard(player1, List.of(eidolon));
        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(eidolon);
    }

    @Test
    @DisplayName("A monocolored spell does not trigger Aurora Eidolon's graveyard ability")
    void monocoloredSpellDoesNotTriggerReturn() {
        AuroraEidolon eidolon = new AuroraEidolon();
        harness.setGraveyard(player1, List.of(eidolon));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(eidolon);
    }
}
