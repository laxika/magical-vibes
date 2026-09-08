package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightningSecuritySergeant.class, Island.class, ActOfTreason.class, LightningBolt.class})
class LightningSecuritySergeantTest extends BaseCardTest {

    private Permanent addLightning() {
        Permanent lightning = addCreatureReady(player1, new LightningSecuritySergeant());
        lightning.setAttacking(true);
        return lightning;
    }

    private Card resolveCombatWithTopCard(Card topCard) {
        addLightning();
        harness.setLibrary(player1, List.of(topCard));
        resolveCombat();
        harness.passBothPriorities();
        return topCard;
    }

    @Test
    @DisplayName("Exiles the top card and lets its controller play it")
    void exilesTopCardAndAllowsControllerToPlayIt() {
        Card topCard = resolveCombatWithTopCard(new Island());

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    @DisplayName("Loses the permission when another player gains control")
    void permissionEndsOnControlChange() {
        Permanent lightning = addLightning();
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        resolveCombat();
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ActOfTreason()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castSorcery(player2, 0, lightning.getId());
        harness.passBothPriorities();

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.exilePlayPermissionSourcePermanents).doesNotContainKey(topCard.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(lightning.getId()));
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    @Test
    @DisplayName("Loses the permission when the source leaves the battlefield")
    void permissionEndsOnSourceLeavingBattlefield() {
        Permanent lightning = addLightning();
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        resolveCombat();
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, lightning.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(lightning.getId()));
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }
}
