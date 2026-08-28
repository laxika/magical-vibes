package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WelcomeHome;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlaxenIntruder.class, WelcomeHome.class, ZuranOrb.class, GrizzlyBears.class})
class FlaxenIntruderTest extends BaseCardTest {

    @Test
    void adventureCreatesThreeBearsAndExilesTheCard() {
        FlaxenIntruder card = new FlaxenIntruder();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        FlaxenIntruder card = new FlaxenIntruder();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void combatDamageMaySacrificeFlaxenIntruderToDestroyArtifactOrEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ZuranOrb());
        Permanent intruder = addCreatureReady(player1, new FlaxenIntruder());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(intruder);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void decliningCombatDamageAbilityKeepsBothPermanents() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ZuranOrb());
        Permanent intruder = addCreatureReady(player1, new FlaxenIntruder());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(intruder);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void combatDamageTargetChoiceOnlyAllowsArtifactsAndEnchantments() {
        Permanent validTarget = harness.addToBattlefieldAndReturn(player2, new ZuranOrb());
        addCreatureReady(player1, new FlaxenIntruder());
        Permanent invalidTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(validTarget.getId()).doesNotContain(invalidTarget.getId());
    }
}
