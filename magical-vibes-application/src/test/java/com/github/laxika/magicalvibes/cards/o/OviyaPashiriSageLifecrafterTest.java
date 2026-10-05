package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.w.WilyBandar;
import com.github.laxika.magicalvibes.cards.w.WeldingSparks;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OviyaPashiriSageLifecrafter.class, WilyBandar.class, WeldingSparks.class})
class OviyaPashiriSageLifecrafterTest extends BaseCardTest {

    @Test
    @DisplayName("First ability creates a 1/1 colorless Servo artifact creature token")
    void firstAbilityCreatesServoToken() {
        Permanent oviya = addCreatureReady(player1, new OviyaPashiriSageLifecrafter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, indexOf(oviya), 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Servo").getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SERVO);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(oviya.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second ability creates a Construct whose power and toughness equal the controlled creature count")
    void secondAbilityCreatesConstructSizedToCreatureCount() {
        Permanent oviya = addCreatureReady(player1, new OviyaPashiriSageLifecrafter());
        addCreatureReady(player1, new WilyBandar());
        addCreatureReady(player1, new WilyBandar());
        addCreatureReady(player2, new WilyBandar());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, indexOf(oviya), 1, null, null);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Construct");
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getPower()).isEqualTo(3);
        assertThat(tokens.getFirst().getCard().getToughness()).isEqualTo(3);
        assertThat(tokens.getFirst().getCard().getColor()).isNull();
        assertThat(tokens.getFirst().getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(tokens.getFirst().getCard().getSubtypes()).contains(CardSubtype.CONSTRUCT);
    }

    @Test
    @DisplayName("Construct size excludes Oviya if she dies before resolution and counts existing tokens")
    void constructCountsRemainingCreaturesAtResolution() {
        Permanent oviya = addCreatureReady(player1, new OviyaPashiriSageLifecrafter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, indexOf(oviya), 0, null, null);
        harness.passBothPriorities();
        oviya.setTapped(false);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, indexOf(oviya), 1, null, null);

        harness.setHand(player2, List.of(new WeldingSparks()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, oviya.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Oviya Pashiri, Sage Lifecrafter");
        Permanent construct = findPermanent(player1, "Construct");
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(1);
        assertThat(countPermanents(player1, "Servo")).isEqualTo(1);
    }

    @Test
    @DisplayName("With no creatures remaining the ability creates a 0/0 Construct that dies")
    void zeroCreatureCountStillCreatesToken() {
        Permanent oviya = addCreatureReady(player1, new OviyaPashiriSageLifecrafter());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, indexOf(oviya), 1, null, null);

        harness.setHand(player2, List.of(new WeldingSparks()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, oviya.getId());
        resolveAllTriggers();

        assertThat(gameLogContains("0/0 Construct creature token enters the battlefield")).isTrue();
        harness.assertNotOnBattlefield(player1, "Construct");
        harness.assertInGraveyard(player1, "Oviya Pashiri, Sage Lifecrafter");
    }

    @Test
    @DisplayName("A Construct keeps its original size when another token is created")
    void constructSizeDoesNotTrackCreatureCount() {
        Permanent oviya = addCreatureReady(player1, new OviyaPashiriSageLifecrafter());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, indexOf(oviya), 1, null, null);
        harness.passBothPriorities();
        Permanent construct = findPermanent(player1, "Construct");
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(1);

        oviya.setTapped(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, indexOf(oviya), 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Servo")).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Both tap abilities are unavailable while Oviya has summoning sickness")
    void abilitiesRequireOviyaToBeReady(int abilityIndex) {
        Permanent oviya = harness.addToBattlefieldAndReturn(player1, new OviyaPashiriSageLifecrafter());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(oviya), abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("summoning sickness");
        assertThat(oviya.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Neither ability can pay its green mana requirement with only colorless mana")
    void abilitiesRequireGreenMana(int abilityIndex) {
        Permanent oviya = addCreatureReady(player1, new OviyaPashiriSageLifecrafter());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(oviya), abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Not enough mana");
        assertThat(oviya.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
