package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UriangerAugurelt.class, Forest.class, GrizzlyBears.class})
class UriangerAugureltTest extends BaseCardTest {

    @Test
    void drawArcanumMayExileTheTopCardFaceDownWithUrianger() {
        Permanent urianger = addCreatureReady(player1, new UriangerAugurelt());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(topCard.getId())).extracting(ExiledCardEntry::faceDown)
                .isEqualTo(true);
        assertThat(gd.findExiledCard(topCard.getId())).extracting(ExiledCardEntry::sourcePermanentId)
                .isEqualTo(urianger.getId());
    }

    @Test
    void decliningDrawArcanumLeavesTheTopCardOnTheLibrary() {
        Permanent urianger = addCreatureReady(player1, new UriangerAugurelt());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(urianger.isTapped()).isTrue();
    }

    @Test
    void playArcanumLetsTheControllerPlayTrackedLandsAndReducedCostSpells() {
        Permanent urianger = addCreatureReady(player1, new UriangerAugurelt());
        Forest land = new Forest();
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player1.getId(), land, urianger.getId());
        gd.addToExile(player1.getId(), spell, urianger.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.exilePlayPermissions)
                .containsEntry(land.getId(), player1.getId())
                .containsEntry(spell.getId(), player1.getId());
        assertThat(gd.exilePlayCostModifiers).containsKey(spell.getId());
        assertThat(gd.exilePlayCostModifiers).doesNotContainKey(land.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, land.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, spell.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertOnBattlefield(player1, "Forest", "Grizzly Bears");
    }

    @Test
    void gainsTwoLifeWhenControllerPlaysLandAndCastsSpellFromExile() {
        addCreatureReady(player1, new UriangerAugurelt());
        Forest land = new Forest();
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player1.getId(), land);
        gd.addToExile(player1.getId(), spell);
        gd.exilePlayPermissions.put(land.getId(), player1.getId());
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        int lifeBefore = gd.getLife(player1.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, land.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        harness.castFromExile(player1, spell.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
    }
}
