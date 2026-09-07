package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvertedIceberg.class, IcebergTitan.class, DarksteelRelic.class, GrizzlyBears.class})
class InvertedIcebergTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by milling a card, then drawing a card")
    void entersByMillingAndDrawing() {
        Card milled = new GrizzlyBears();
        Card drawn = new DarksteelRelic();
        harness.setHand(player1, List.of(new InvertedIceberg()));
        harness.setLibrary(player1, List.of(milled, drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milled);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Craft exiles another artifact and returns Iceberg Titan transformed")
    void craftsIntoIcebergTitan() {
        Permanent iceberg = harness.addToBattlefieldAndReturn(player1, new InvertedIceberg());
        Permanent material = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(iceberg);
        assertThat(gd.findExiledCard(material.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isTransformed() && permanent.getCard() instanceof IcebergTitan);
    }

    @Test
    @DisplayName("Iceberg Titan offers an artifact or creature to tap when it attacks")
    void attackTriggerTapsArtifactOrCreature() {
        InvertedIceberg front = new InvertedIceberg();
        Permanent titan = new Permanent(front);
        titan.setCard(front.getBackFaceCard());
        titan.setTransformed(true);
        titan.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(titan);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());

        declareAttackers(player1, List.of(0));
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(titan.getId(), target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
    }
}
