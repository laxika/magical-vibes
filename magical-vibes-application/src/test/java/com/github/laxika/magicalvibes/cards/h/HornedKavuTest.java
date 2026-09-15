package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.ArcticMerfolk;
import com.github.laxika.magicalvibes.cards.t.ThornscapeFamiliar;
import com.github.laxika.magicalvibes.cards.t.ThunderscapeFamiliar;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HornedKavu.class, ThornscapeFamiliar.class, ThunderscapeFamiliar.class, ArcticMerfolk.class})
class HornedKavuTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers a non-targeting choice among red or green creatures you control")
    void etbOffersRedOrGreenCreatures() {
        UUID goblinId = harness.addToBattlefieldAndReturn(player1, new ThunderscapeFamiliar()).getId();
        UUID familiarId = harness.addToBattlefieldAndReturn(player1, new ThornscapeFamiliar()).getId();
        UUID merfolkId = harness.addToBattlefieldAndReturn(player1, new ArcticMerfolk()).getId();
        harness.addToBattlefield(player2, new ThunderscapeFamiliar());

        castHornedKavu();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        UUID kavuId = harness.getPermanentId(player1, "Horned Kavu");
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(goblinId, familiarId, kavuId);
        assertThat(choice.validIds()).doesNotContain(merfolkId);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("The chosen red or green creature returns to its owner's hand")
    void chosenCreatureReturnsToHand() {
        harness.addToBattlefield(player1, new ThunderscapeFamiliar());
        harness.addToBattlefield(player1, new ThornscapeFamiliar());

        castHornedKavu();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Thornscape Familiar"));

        harness.assertInHand(player1, "Thornscape Familiar");
        harness.assertOnBattlefield(player1, "Thunderscape Familiar");
        harness.assertOnBattlefield(player1, "Horned Kavu");
    }

    @Test
    @DisplayName("The chosen creature returns to its owner's hand even when it is controlled by another player")
    void chosenCreatureReturnsToItsOwnersHand() {
        ThunderscapeFamiliar card = new ThunderscapeFamiliar();
        card.setOwnerId(player2.getId());
        var stolen = harness.addToBattlefieldAndReturn(player2, card);
        UUID stolenId = stolen.getId();
        gd.stolenCreatures.put(stolenId, player2.getId());
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player1.getId(), stolen,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT,
                        null, "Test setup"));

        castHornedKavu();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(stolenId);
        harness.handlePermanentChosen(player1, stolenId);

        harness.assertInHand(player2, "Thunderscape Familiar");
        harness.assertNotInHand(player1, "Thunderscape Familiar");
        harness.assertNotOnBattlefield(player1, "Thunderscape Familiar");
        harness.assertOnBattlefield(player1, "Horned Kavu");
    }

    @Test
    @DisplayName("Horned Kavu returns itself when it is the only eligible creature")
    void returnsItselfWhenAlone() {
        castHornedKavu();
        harness.passBothPriorities();

        UUID kavuId = harness.getPermanentId(player1, "Horned Kavu");
        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).containsExactly(kavuId);

        harness.handlePermanentChosen(player1, kavuId);

        harness.assertInHand(player1, "Horned Kavu");
        harness.assertNotOnBattlefield(player1, "Horned Kavu");
    }

    private void castHornedKavu() {
        harness.castFromHand(player1, new HornedKavu(), "{R}{G}");
        harness.passBothPriorities();
    }
}
