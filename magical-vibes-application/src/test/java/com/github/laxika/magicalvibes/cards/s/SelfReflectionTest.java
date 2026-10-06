package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SelfReflection.class, SavannahLions.class})
class SelfReflectionTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy of target creature you control")
    void createsTokenCopyOfCreatureYouControl() {
        harness.addToBattlefield(player1, new SavannahLions());
        harness.setHand(player1, List.of(new SelfReflection()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        UUID targetId = harness.getPermanentId(player1, "Savannah Lions");
        harness.castAndResolveSorcery(player1, 0, targetId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .filteredOn(p -> p.getCard().getName().equals("Savannah Lions"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Flashback creates a token copy and exiles Self-Reflection")
    void flashbackCreatesTokenCopyAndExilesSpell() {
        harness.addToBattlefield(player1, new SavannahLions());
        harness.setGraveyard(player1, List.of(new SelfReflection()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        UUID targetId = harness.getPermanentId(player1, "Savannah Lions");
        harness.castAndResolveFlashback(player1, 0, targetId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .filteredOn(p -> p.getCard().getName().equals("Savannah Lions"))
                .hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Self-Reflection"));
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new SavannahLions());
        harness.setHand(player1, List.of(new SelfReflection()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        UUID targetId = harness.getPermanentId(player2, "Savannah Lions");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Copy does not inherit tapped status or counters")
    void copyDoesNotInheritPermanentState() {
        var original = harness.addToBattlefieldAndReturn(player1, new SavannahLions());
        original.setTapped(true);
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new SelfReflection()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, original.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .singleElement()
                .satisfies(copy -> {
                    assertThat(copy.isTapped()).isFalse();
                    assertThat(copy.getPlusOnePlusOneCounters()).isZero();
                    assertThat(copy.isSummoningSick()).isTrue();
                });
        harness.assertInGraveyard(player1, "Self-Reflection");
    }

    @Test
    @DisplayName("Losing control of the target makes the spell fail to resolve")
    void targetMustStillBeControlledAtResolution() {
        var original = harness.addToBattlefieldAndReturn(player1, new SavannahLions());
        harness.setHand(player1, List.of(new SelfReflection()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castSorcery(player1, 0, original.getId());

        gd.playerBattlefields.get(player1.getId()).remove(original);
        gd.playerBattlefields.get(player2.getId()).add(original);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(original);
        harness.assertInGraveyard(player1, "Self-Reflection");
    }

    @Test
    @DisplayName("Flashback is exiled even when its target becomes illegal")
    void flashbackExiledWhenTargetBecomesIllegal() {
        var original = harness.addToBattlefieldAndReturn(player1, new SavannahLions());
        harness.setGraveyard(player1, List.of(new SelfReflection()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castFlashback(player1, 0, original.getId());

        gd.playerBattlefields.get(player1.getId()).remove(original);
        gd.playerBattlefields.get(player2.getId()).add(original);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(original);
        harness.assertNotInGraveyard(player1, "Self-Reflection");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Self-Reflection"));
    }
}
