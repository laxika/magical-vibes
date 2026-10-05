package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RadiantDestiny.class, RaptorCompanion.class})
class RadiantDestinyTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type gives matching creatures +1/+1")
    void boostsCreaturesOfChosenType() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        Permanent destiny = addReadyDestiny(CardSubtype.DINOSAUR);

        assertThat(destiny.getChosenSubtype()).isEqualTo(CardSubtype.DINOSAUR);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("With the city's blessing, matching creatures also have vigilance")
    void grantsVigilanceWithCityBlessing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        addReadyDestiny(CardSubtype.DINOSAUR);
        gd.playersWithCityBlessing.add(player1.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Without the city's blessing, matching creatures do not have vigilance")
    void doesNotGrantVigilanceWithoutCityBlessing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        addReadyDestiny(CardSubtype.DINOSAUR);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Creatures of another type do not receive Radiant Destiny's bonuses")
    void doesNotAffectDifferentType() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        addReadyDestiny(CardSubtype.ELF);
        gd.playersWithCityBlessing.add(player1.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Resolving Radiant Destiny prompts for a creature type")
    void resolvingPromptsForSubtypeChoice() {
        harness.setHand(player1, List.of(new RadiantDestiny()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Radiant Destiny itself counts as the tenth permanent for ascend")
    void grantsBlessingWhenEnteringAsTenthPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new RaptorCompanion());
        }

        castDestinyAndChooseDinosaur();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ascend grants the blessing when a tenth permanent enters later")
    void grantsBlessingWhenReachingTenPermanentsLater() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new RaptorCompanion());
        }
        castDestinyAndChooseDinosaur();
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();

        Permanent tenth = harness.enterBattlefieldAndReturn(player1, new RaptorCompanion());

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(tenth);
        harness.runStateBasedActions();
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Opposing creatures of the chosen type receive neither bonus")
    void doesNotAffectOpponentsCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        addReadyDestiny(CardSubtype.DINOSAUR);
        gd.playersWithCityBlessing.add(player1.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's blessing does not enable Radiant Destiny's vigilance")
    void opponentsBlessingDoesNotGrantVigilance() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        addReadyDestiny(CardSubtype.DINOSAUR);
        gd.playersWithCityBlessing.add(player2.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Completing the type choice boosts both existing and later creatures")
    void chosenTypeAppliesToExistingAndLaterCreatures() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        castDestinyAndChooseDinosaur();
        Permanent later = harness.enterBattlefieldAndReturn(player1, new RaptorCompanion());

        assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, existing)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(2);
    }

    private void castDestinyAndChooseDinosaur() {
        harness.setHand(player1, List.of(new RadiantDestiny()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "DINOSAUR");
    }

    private Permanent addReadyDestiny(CardSubtype chosenSubtype) {
        Permanent destiny = harness.addToBattlefieldAndReturn(player1, new RadiantDestiny());
        destiny.setChosenSubtype(chosenSubtype);
        return destiny;
    }
}
