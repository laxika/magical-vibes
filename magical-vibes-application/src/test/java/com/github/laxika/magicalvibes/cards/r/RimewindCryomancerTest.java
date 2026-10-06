package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AurochsHerd;
import com.github.laxika.magicalvibes.cards.s.Skred;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.cards.s.ScryingSheets;
import com.github.laxika.magicalvibes.cards.s.StromgaldCrusader;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RimewindCryomancer.class, StromgaldCrusader.class, SnowCoveredIsland.class, Skred.class,
        ScryingSheets.class, AurochsHerd.class})
class RimewindCryomancerTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a non-targeted activated ability with four snow permanents")
    void countersActivatedAbilityWithFourSnowPermanents() {
        addCreatureReady(player1, new RimewindCryomancer());
        addSnowPermanents(player1, 4);
        Permanent crusader = addCreatureReady(player2, new StromgaldCrusader());
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, null);
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, crusader.getCard().getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, crusader, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Requires four snow permanents you control")
    void requiresFourSnowPermanentsYouControl() {
        addCreatureReady(player1, new RimewindCryomancer());
        addSnowPermanents(player1, 3);
        addSnowPermanents(player2, 1);
        Permanent crusader = addCreatureReady(player2, new StromgaldCrusader());
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 1, null, null);
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, crusader.getCard().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("four or more snow permanents");

        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, crusader, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a spell")
    void cannotTargetSpell() {
        Permanent cryomancer = addCreatureReady(player1, new RimewindCryomancer());
        addSnowPermanents(player1, 4);

        Skred skred = new Skred();
        harness.setHand(player2, List.of(skred));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, cryomancer.getId());
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, skred.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a mana ability")
    void cannotTargetManaAbility() {
        addCreatureReady(player1, new RimewindCryomancer());
        addSnowPermanents(player1, 4);
        Permanent scryingSheets = harness.addToBattlefieldAndReturn(player2, new ScryingSheets());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, null, null);
        assertThat(harness.getGameData().stack).isEmpty();
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, scryingSheets.getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Snow requirement is checked only when activating")
    void countersAfterLosingSnowPermanents() {
        Permanent cryomancer = addCreatureReady(player1, new RimewindCryomancer());
        addSnowPermanents(player1, 4);
        Permanent crusader = addCreatureReady(player2, new StromgaldCrusader());
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.activateAbility(player2, 0, null, null);
        UUID target = gd.stack.getLast().getTargetableId();
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target);
        assertThat(cryomancer.isTapped()).isTrue();
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p != cryomancer);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, crusader, Keyword.FLYING)).isFalse();
        harness.assertOnBattlefield(player2, "Stromgald Crusader");
        harness.assertNotInGraveyard(player2, "Stromgald Crusader");
    }

    @Test
    @DisplayName("Can counter your own activated ability")
    void countersOwnActivatedAbility() {
        addCreatureReady(player1, new RimewindCryomancer());
        Permanent crusader = addCreatureReady(player1, new StromgaldCrusader());
        addSnowPermanents(player1, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 1, null, null);
        UUID target = gd.stack.getLast().getTargetableId();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, crusader, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Counters only the selected ability when a source has multiple abilities on the stack")
    void countersOnlySelectedStackObject() {
        addCreatureReady(player1, new RimewindCryomancer());
        addSnowPermanents(player1, 4);
        Permanent crusader = addCreatureReady(player2, new StromgaldCrusader());
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.activateAbility(player2, 0, 0, null, null);
        UUID flyingAbility = gd.stack.getLast().getTargetableId();
        harness.activateAbility(player2, 0, 1, null, null);
        UUID pumpAbility = gd.stack.getLast().getTargetableId();
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, flyingAbility);
        harness.passBothPriorities();

        assertThat(gd.stack).extracting(entry -> entry.getTargetableId()).containsExactly(pumpAbility);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, crusader, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a triggered ability")
    void cannotTargetTriggeredAbility() {
        addCreatureReady(player1, new RimewindCryomancer());
        addSnowPermanents(player1, 4);
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new AurochsHerd(), "{5}{G}");
        harness.passBothPriorities();
        UUID trigger = gd.stack.getLast().getTargetableId();
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, trigger))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("activated ability");
    }

    private void addSnowPermanents(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new SnowCoveredIsland());
        }
    }
}
