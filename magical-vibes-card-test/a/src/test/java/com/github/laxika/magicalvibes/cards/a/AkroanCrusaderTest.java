package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.c.CoordinatedAssault;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkroanCrusader.class, Shock.class, GiantGrowth.class, CoordinatedAssault.class})
class AkroanCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Akroan Crusader creates a hasty Soldier token")
    void castingSpellThatTargetsCrusaderCreatesHastySoldier() {
        harness.addToBattlefield(player1, new AkroanCrusader());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID crusaderId = harness.getPermanentId(player1, "Akroan Crusader");
        harness.castAndResolveInstant(player1, 0, crusaderId);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Soldier"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Akroan Crusader")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new AkroanCrusader());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's spell that targets Akroan Crusader does not trigger it")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new AkroanCrusader());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID crusaderId = harness.getPermanentId(player1, "Akroan Crusader");
        harness.castAndResolveInstant(player2, 0, crusaderId);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A spell targeting two Crusaders creates one token for each before the spell resolves")
    void targetingTwoCrusadersTriggersEachOnce() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AkroanCrusader());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AkroanCrusader());
        harness.setHand(player1, List.of(new CoordinatedAssault()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(first.getPowerModifier()).isZero();
        assertThat(second.getPowerModifier()).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
        harness.passBothPriorities();
        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(second.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the Crusader targeted by a spell triggers heroic")
    void untargetedCrusaderDoesNotTrigger() {
        Permanent targeted = harness.addToBattlefieldAndReturn(player1, new AkroanCrusader());
        harness.addToBattlefield(player1, new AkroanCrusader());
        harness.setHand(player1, List.of(new CoordinatedAssault()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, targeted.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("Casting Coordinated Assault with no targets does not trigger heroic")
    void spellWithNoTargetsDoesNotTrigger() {
        harness.addToBattlefield(player1, new AkroanCrusader());
        harness.setHand(player1, List.of(new CoordinatedAssault()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.<UUID>of());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Heroic still creates a token after its Crusader is killed in response")
    void triggerResolvesAfterCrusaderDies() {
        Permanent crusader = harness.addToBattlefieldAndReturn(player1, new AkroanCrusader());
        harness.setHand(player1, List.of(new CoordinatedAssault()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, crusader.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, crusader.getId());
        harness.assertInGraveyard(player1, "Akroan Crusader");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.getCard().getName()).isEqualTo("Soldier");
                });
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }
}
