package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.i.IslandSanctuary;
import com.github.laxika.magicalvibes.cards.d.DreadfulApathy;
import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.cards.o.OmenOfTheSun;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlickerOfFate.class, GrizzlyBears.class, Island.class, IslandSanctuary.class,
        DreadfulApathy.class, NyxbornCourser.class, OmenOfTheSun.class})
class FlickerOfFateTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles and immediately returns a target creature under its owner's control")
    void flickersTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlickerOfFate()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castAndResolveInstant(player1, 0, bearsId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getPermanentId(player2, "Grizzly Bears")).isNotEqualTo(bearsId);
    }

    @Test
    @DisplayName("Exiles and immediately returns a target enchantment")
    void flickersTargetEnchantment() {
        harness.addToBattlefield(player1, new IslandSanctuary());
        harness.setHand(player1, List.of(new FlickerOfFate()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID sanctuaryId = harness.getPermanentId(player1, "Island Sanctuary");

        harness.castAndResolveInstant(player1, 0, sanctuaryId);

        harness.assertOnBattlefield(player1, "Island Sanctuary");
        assertThat(harness.getPermanentId(player1, "Island Sanctuary")).isNotEqualTo(sanctuaryId);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new FlickerOfFate()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID islandId = harness.getPermanentId(player1, "Island");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, islandId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or enchantment");
    }

    @Test
    void returnsBorrowedCreatureToItsOwner() {
        NyxbornCourser card = new NyxbornCourser();
        card.setOwnerId(player2.getId());
        var original = harness.addToBattlefieldAndReturn(player1, card);
        gd.stolenCreatures.put(original.getId(), player2.getId());
        harness.setHand(player1, List.of(new FlickerOfFate()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, original.getId());

        harness.assertNotOnBattlefield(player1, "Nyxborn Courser");
        harness.assertOnBattlefield(player2, "Nyxborn Courser");
        assertThat(harness.getPermanentId(player2, "Nyxborn Courser")).isNotEqualTo(original.getId());
    }

    @Test
    void returnsUntappedWithoutOldCounters() {
        var original = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        original.tap();
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new FlickerOfFate()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, original.getId());

        var returned = findPermanent(player1, "Nyxborn Courser");
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void returningEnchantmentTriggersItsEnterAbility() {
        var original = harness.addToBattlefieldAndReturn(player2, new OmenOfTheSun());
        harness.setHand(player1, List.of(new FlickerOfFate()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, original.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 22);
        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList()).hasSize(2);
        assertThat(harness.getPermanentId(player2, "Omen of the Sun")).isNotEqualTo(original.getId());
    }

    @Test
    void exiledTokenDoesNotReturn() {
        harness.setHand(player1, List.of(new OmenOfTheSun(), new FlickerOfFate()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        var token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();

        harness.castAndResolveInstant(player1, 0, token.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }

    @Test
    void returningAuraAttachesToTheOnlyLegalCreature() {
        var creature = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        var original = harness.addToBattlefieldAndReturn(player1, new DreadfulApathy());
        original.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new FlickerOfFate()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, original.getId());

        harness.assertOnBattlefield(player1, "Dreadful Apathy");
        var returned = findPermanent(player1, "Dreadful Apathy");
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.getAttachedTo()).isEqualTo(creature.getId());
        harness.assertNotInGraveyard(player1, "Dreadful Apathy");
    }

    @Test
    void targetLeavingBattlefieldBeforeResolutionDoesNotReturn() {
        var creature = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        var aura = harness.addToBattlefieldAndReturn(player1, new DreadfulApathy());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new FlickerOfFate()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, creature.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Nyxborn Courser");
        harness.assertNotOnBattlefield(player1, "Dreadful Apathy");
        harness.assertInGraveyard(player1, "Dreadful Apathy");
        harness.assertInGraveyard(player1, "Flicker of Fate");
        assertThat(gd.findExiledCard(creature.getCard().getId())).isNotNull();
    }
}
