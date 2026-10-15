package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BladeOfTheSixthPride;
import com.github.laxika.magicalvibes.cards.f.Foresee;
import com.github.laxika.magicalvibes.cards.l.LucentLiminid;
import com.github.laxika.magicalvibes.cards.r.Refurbish;
import com.github.laxika.magicalvibes.cards.s.SproutSwarm;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CloudKey.class, BladeOfTheSixthPride.class, Foresee.class, LucentLiminid.class,
        Refurbish.class, SproutSwarm.class})
class CloudKeyTest extends BaseCardTest {

    @Test
    void choosesOneOfThePrintedCardTypes() {
        castCloudKey();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly(
                CardType.CREATURE.name(),
                CardType.ENCHANTMENT.name(),
                CardType.SORCERY.name(),
                CardType.INSTANT.name(),
                CardType.ARTIFACT.name());
    }

    @Test
    void spellsOfChosenTypeCostOneLess() {
        castCloudKey();
        harness.handleListChoice(player1, CardType.CREATURE.name());

        harness.castFromHand(player1, new BladeOfTheSixthPride(), "{W}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void spellsOfOtherTypesAreNotReduced() {
        castCloudKey();
        harness.handleListChoice(player1, CardType.CREATURE.name());

        harness.setHand(player1, List.of(new Foresee()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reductionDoesNotApplyToOpponents() {
        castCloudKey();
        harness.handleListChoice(player1, CardType.CREATURE.name());

        harness.setHand(player2, List.of(new BladeOfTheSixthPride()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void artifactSpellsCostOneLess() {
        castCloudKey();
        harness.handleListChoice(player1, CardType.ARTIFACT.name());

        harness.castFromHand(player1, new CloudKey(), "{2}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardType.CREATURE.name());
        assertThat(findPermanents(player1, "Cloud Key")).hasSize(2);
    }

    @Test
    void sorcerySpellsCostOneLess() {
        castCloudKey();
        harness.handleListChoice(player1, CardType.SORCERY.name());

        harness.castFromHand(player1, new Foresee(), "{2}{U}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void instantSpellsCostOneLess() {
        castCloudKey();
        harness.handleListChoice(player1, CardType.INSTANT.name());

        harness.castFromHand(player1, new SproutSwarm(), "{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Saproling");
    }

    @ParameterizedTest
    @EnumSource(value = CardType.class, names = {"CREATURE", "ENCHANTMENT"})
    void spellWithMultipleTypesMatchesEitherChosenType(CardType chosenType) {
        castCloudKey();
        harness.handleListChoice(player1, chosenType.name());

        harness.castFromHand(player1, new LucentLiminid(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lucent Liminid");
    }

    @Test
    void reductionDoesNotPayColoredMana() {
        castCloudKey();
        harness.handleListChoice(player1, CardType.CREATURE.name());

        assertThatThrownBy(() -> harness.castFromHand(player1, new BladeOfTheSixthPride(), "{1}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reductionsFromMultipleKeysAreCumulative() {
        castCloudKey();
        harness.handleListChoice(player1, CardType.SORCERY.name());
        castCloudKey();
        harness.handleListChoice(player1, CardType.SORCERY.name());

        harness.castFromHand(player1, new Foresee(), "{1}{U}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void multipleKeysKeepIndependentChoices() {
        castCloudKey();
        harness.handleListChoice(player1, CardType.CREATURE.name());
        castCloudKey();
        harness.handleListChoice(player1, CardType.SORCERY.name());

        harness.castFromHand(player1, new BladeOfTheSixthPride(), "{W}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Blade of the Sixth Pride");

        harness.castFromHand(player1, new Foresee(), "{2}{U}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void choosesTypeWhenReturnedFromGraveyard() {
        CloudKey key = new CloudKey();
        harness.setGraveyard(player1, List.of(key));
        harness.setHand(player1, List.of(new Refurbish()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, key.getId());
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        harness.handleListChoice(player1, CardType.CREATURE.name());
        harness.assertOnBattlefield(player1, "Cloud Key");

        harness.castFromHand(player1, new BladeOfTheSixthPride(), "{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Blade of the Sixth Pride");
    }

    private void castCloudKey() {
        harness.castFromHand(player1, new CloudKey(), "{3}");
        harness.passBothPriorities();
    }
}
