package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.f.FallingStar;
import com.github.laxika.magicalvibes.cards.p.PsionicEntity;
import com.github.laxika.magicalvibes.cards.p.PsychicPurge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Silhouette.class, BarbaryApes.class, FallingStar.class, PsionicEntity.class, PsychicPurge.class})
class SilhouetteTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage from a spell targeting the protected creature")
    void preventsTargetedSpellDamage() {
        Permanent apes = protectCreature();

        harness.setHand(player1, List.of(new PsychicPurge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, apes.getId());

        assertThat(apes.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Barbary Apes");
    }

    @Test
    @DisplayName("Prevents damage caused by an ability targeting the protected creature")
    void preventsTargetedAbilityDamage() {
        Permanent apes = protectCreature();
        addCreatureReady(player1, new PsionicEntity());

        harness.activateAbility(player1, 0, null, apes.getId());
        harness.passBothPriorities();

        assertThat(apes.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Barbary Apes");
    }

    @Test
    @DisplayName("Does not prevent damage from a spell that does not target the creature")
    void doesNotPreventUntargetedSpellDamage() {
        protectCreature();

        harness.setHand(player1, List.of(new FallingStar()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, List.of());

        harness.assertNotOnBattlefield(player2, "Barbary Apes");
    }

    @Test
    @DisplayName("Protection expires at the end of the turn")
    void protectionExpiresAtEndOfTurn() {
        Permanent apes = protectCreature();

        harness.setHand(player1, List.of(new PsychicPurge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, apes.getId());
        assertThat(apes.getMarkedDamage()).isZero();

        harness.setHand(player2, List.of());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.DECLARE_ATTACKERS);
        declareAttackers(player2, List.of());
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new PsychicPurge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, apes.getId());

        assertThat(apes.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Barbary Apes");
    }

    private Permanent protectCreature() {
        Permanent apes = addCreatureReady(player2, new BarbaryApes());
        harness.setHand(player1, List.of(new Silhouette()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, apes.getId());
        return apes;
    }
}
