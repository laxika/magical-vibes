package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GravespawnSovereign.class, ScatheZombies.class, GrizzlyBears.class, HolyDay.class})
class GravespawnSovereignTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping five Zombies puts a creature card from any graveyard onto the battlefield under your control")
    void reanimatesCreatureFromAnyGraveyardUnderYourControl() {
        Permanent sovereign = addCreatureReady(player1, new GravespawnSovereign());
        addZombies(player1, 4);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));

        int sovereignIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sovereign);
        harness.activateAbility(player1, sovereignIndex, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(sovereign.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
    }

    @Test
    @DisplayName("The ability cannot be activated without five untapped Zombies")
    void requiresFiveUntappedZombies() {
        Permanent sovereign = addCreatureReady(player1, new GravespawnSovereign());
        addZombies(player1, 3);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));

        int sovereignIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sovereign);
        assertThatThrownBy(() ->
                harness.activateAbility(player1, sovereignIndex, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a noncreature card")
    void cannotTargetNoncreatureCard() {
        Permanent sovereign = addCreatureReady(player1, new GravespawnSovereign());
        addZombies(player1, 4);
        Card target = new HolyDay();
        harness.setGraveyard(player2, List.of(target));

        int sovereignIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sovereign);
        assertThatThrownBy(() ->
                harness.activateAbility(player1, sovereignIndex, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addZombies(com.github.laxika.magicalvibes.model.Player player, int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player, new ScatheZombies());
        }
    }
}
