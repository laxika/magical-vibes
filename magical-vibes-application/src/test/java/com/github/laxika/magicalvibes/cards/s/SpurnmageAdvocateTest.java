package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpurnmageAdvocate.class, GrizzlyBears.class, LightningBolt.class, Shock.class})
class SpurnmageAdvocateTest extends BaseCardTest {

    @Test
    @DisplayName("Returns two cards from an opponent's graveyard and destroys an attacking creature")
    void returnsCardsAndDestroysAttacker() {
        Permanent advocate = addReadyAdvocate();
        Card first = new LightningBolt();
        Card second = new Shock();
        Permanent attacker = addAttacker();
        harness.setGraveyard(player2, List.of(first, second));

        harness.activateAbilityWithMultiTargets(player1, index(advocate), 0,
                List.of(first.getId(), second.getId(), attacker.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getId)
                .contains(first.getId(), second.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(attacker.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(attacker.getCard().getId()));
        assertThat(advocate.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Rejects a nonattacking creature as the destruction target")
    void rejectsNonattackingCreature() {
        Permanent advocate = addReadyAdvocate();
        Card first = new LightningBolt();
        Card second = new Shock();
        Permanent bystander = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player2, List.of(first, second));

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, index(advocate), 0,
                List.of(first.getId(), second.getId(), bystander.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyAdvocate() {
        Permanent advocate = new Permanent(new SpurnmageAdvocate());
        advocate.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(advocate);
        return advocate;
    }

    private Permanent addAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        return attacker;
    }

    private int index(Permanent advocate) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(advocate);
    }
}
