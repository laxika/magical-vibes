package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.e.EssenceInfusion;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilverquillPledgemage.class, GiantGrowth.class, GrizzlyBears.class, BarkshellBlessing.class, EssenceInfusion.class})
class SilverquillPledgemageTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant lets Silverquill Pledgemage gain flying")
    void castingInstantGrantsFlying() {
        Permanent pledgemage = addCreatureReady(player1, new SilverquillPledgemage());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, pledgemage.getId());
        resolveAllTriggers();
        harness.handleListChoice(player1, "FLYING");

        assertThat(gqs.hasKeyword(gd, pledgemage, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, pledgemage, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Copying an instant lets Silverquill Pledgemage gain lifelink")
    void copyingInstantGrantsLifelink() {
        Permanent pledgemage = addCreatureReady(player1, new SilverquillPledgemage());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, pledgemage.getId(),
                List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.handleListChoice(player1, "LIFELINK");
        resolveAllTriggers();
        harness.handleListChoice(player1, "LIFELINK");

        assertThat(gqs.hasKeyword(gd, pledgemage, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, pledgemage, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The magecraft keyword wears off at end of turn")
    void keywordWearsOffAtEndOfTurn() {
        Permanent pledgemage = addCreatureReady(player1, new SilverquillPledgemage());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, pledgemage.getId());
        resolveAllTriggers();
        harness.handleListChoice(player1, "FLYING");
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pledgemage, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, pledgemage, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Casting a sorcery grants the chosen keyword before the spell resolves")
    void castingSorceryGrantsFlying() {
        Permanent pledgemage = addCreatureReady(player1, new SilverquillPledgemage());
        harness.setHand(player1, List.of(new EssenceInfusion()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0, pledgemage.getId());
        resolveAllTriggers();
        harness.handleListChoice(player1, "FLYING");

        assertThat(gqs.hasKeyword(gd, pledgemage, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Separate magecraft triggers can grant both flying and lifelink")
    void separateTriggersCanGrantBothKeywords() {
        Permanent pledgemage = addCreatureReady(player1, new SilverquillPledgemage());
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, pledgemage.getId());
        resolveAllTriggers();
        harness.handleListChoice(player1, "FLYING");
        resolveAllTriggers();
        harness.castInstant(player1, 0, pledgemage.getId());
        resolveAllTriggers();
        harness.handleListChoice(player1, "LIFELINK");

        assertThat(gqs.hasKeyword(gd, pledgemage, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, pledgemage, Keyword.LIFELINK)).isTrue();
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, pledgemage, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, pledgemage, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("An opponent's instant does not trigger magecraft")
    void opponentsInstantDoesNotTrigger() {
        Permanent pledgemage = addCreatureReady(player1, new SilverquillPledgemage());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, pledgemage.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.hasKeyword(gd, pledgemage, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, pledgemage, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Casting a creature does not trigger magecraft")
    void creatureSpellDoesNotTrigger() {
        Permanent pledgemage = addCreatureReady(player1, new SilverquillPledgemage());
        harness.setHand(player1, List.of(new SilverquillPledgemage()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.hasKeyword(gd, pledgemage, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, pledgemage, Keyword.LIFELINK)).isFalse();
    }
}