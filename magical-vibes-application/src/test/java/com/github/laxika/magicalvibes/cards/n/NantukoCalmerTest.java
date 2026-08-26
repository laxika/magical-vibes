package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AshnodsAltar;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NantukoCalmer.class, AshnodsAltar.class, GloriousAnthem.class, GrizzlyBears.class})
class NantukoCalmerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 while its controller has seven cards in their graveyard")
    void getsBoostAtThreshold() {
        fillGraveyard(player1, 7);
        Permanent calmer = harness.addToBattlefieldAndReturn(player1, new NantukoCalmer());

        assertThat(gqs.getEffectivePower(gd, calmer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, calmer)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not get the threshold boost below seven cards")
    void noBoostBelowThreshold() {
        fillGraveyard(player1, 6);
        Permanent calmer = harness.addToBattlefieldAndReturn(player1, new NantukoCalmer());

        assertThat(gqs.getEffectivePower(gd, calmer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, calmer)).isEqualTo(3);
    }

    @Test
    @DisplayName("Pays mana, taps, and sacrifices itself to destroy an enchantment")
    void destroysTargetEnchantment() {
        Permanent calmer = harness.addToBattlefieldAndReturn(player1, new NantukoCalmer());
        calmer.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(calmer), null,
                target.getId());
        harness.passBothPriorities();

        assertThat(calmer.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Nantuko Calmer");
        harness.assertInGraveyard(player1, "Nantuko Calmer");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent calmer = harness.addToBattlefieldAndReturn(player1, new NantukoCalmer());
        calmer.setSummoningSick(false);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(calmer),
                null,
                creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new AshnodsAltar());
        }
        harness.setGraveyard(player, cards);
    }
}
