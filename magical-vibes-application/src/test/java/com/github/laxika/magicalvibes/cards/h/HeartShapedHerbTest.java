package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeartShapedHerb.class, GrizzlyBears.class, Shock.class})
class HeartShapedHerbTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents one damage from each source to its controller")
    void preventsOneDamagePerSource() {
        harness.addToBattlefield(player1, new HeartShapedHerb());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Sacrifices the artifact and returns the chosen creature with three counters")
    void sacrificesArtifactAndReturnsCreatureWithCounters() {
        Permanent herb = harness.addToBattlefieldAndReturn(player1, new HeartShapedHerb());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Heart-Shaped Herb");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getCard().getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(herb.getCard());
    }

    @Test
    @DisplayName("Declining the optional sacrifice leaves the creature alone")
    void decliningOptionalSacrificeDoesNothing() {
        harness.addToBattlefield(player1, new HeartShapedHerb());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.monarchPlayerId).isNull();
    }

    @Test
    @DisplayName("Sacrificing a token does not return another creature card")
    void sacrificingTokenDoesNotReturnAnotherCreature() {
        harness.addToBattlefield(player1, new HeartShapedHerb());
        Card creatureCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creatureCard));
        Card tokenCard = new Card();
        tokenCard.setName("Bear Token");
        tokenCard.setOwnerId(player1.getId());
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setManaCost("");
        tokenCard.setColor(CardColor.GREEN);
        tokenCard.setPower(2);
        tokenCard.setToughness(2);
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, token.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(creatureCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creatureCard);
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }
}
