package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CrossbonesMaliciousMercenary;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Thunderbolts Conspiracy")
@CardUsed({ThunderboltsConspiracy.class, CrossbonesMaliciousMercenary.class,
        GrizzlyBears.class, Murder.class})
class ThunderboltsConspiracyTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a Villain with a finality counter and Hero subtype")
    void returnsVillainWithFinalityCounterAndHeroSubtype() {
        harness.addToBattlefield(player1, new ThunderboltsConspiracy());
        Permanent villain = addCreatureReady(player1, new CrossbonesMaliciousMercenary());

        destroyWithMurder(player2, villain.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanentByCardId(villain.getCard().getId());
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.VILLAIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.HERO)).isTrue();
        harness.assertNotInGraveyard(player1, "Crossbones, Malicious Mercenary");
    }

    @Test
    @DisplayName("Does not return a non-Villain creature")
    void doesNotReturnNonVillain() {
        harness.addToBattlefield(player1, new ThunderboltsConspiracy());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        destroyWithMurder(player2, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    @Test
    @DisplayName("A returned creature with a finality counter is exiled when it dies again")
    void finalityCounterExilesReturnedCreature() {
        harness.addToBattlefield(player1, new ThunderboltsConspiracy());
        Permanent villain = addCreatureReady(player1, new CrossbonesMaliciousMercenary());

        destroyWithMurder(player2, villain.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanentByCardId(villain.getCard().getId());
        destroyWithMurder(player2, returned.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Crossbones, Malicious Mercenary");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(villain.getCard().getId()));
    }

    private void destroyWithMurder(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 3);

        harness.getGameService().playCard(harness.getGameData(), caster, 0, 0, targetId, null);
        harness.passBothPriorities();
    }

    private Permanent findPermanentByCardId(UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }
}
