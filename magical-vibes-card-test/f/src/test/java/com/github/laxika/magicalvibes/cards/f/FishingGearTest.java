package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FishingGear.class, GrizzlyBears.class, Shock.class})
class FishingGearTest extends BaseCardTest {

    @Test
    @DisplayName("Puts an exiled permanent from the damaged player's library onto the battlefield under your control")
    void putsPermanentOntoBattlefieldWhenAccepted() {
        Permanent gear = addReadyGear();
        Permanent creature = addReadyCreature();
        gear.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, topCard)).isNotNull();
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(findPermanents(player1, "Fish")).isEmpty();
    }

    @Test
    @DisplayName("Creates a Fish when the exiled permanent is declined")
    void createsFishWhenPermanentIsDeclined() {
        Permanent gear = addReadyGear();
        Permanent creature = addReadyCreature();
        gear.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, topCard)).isNull();
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(findPermanents(player1, "Fish")).hasSize(1);
    }

    @Test
    @DisplayName("Creates a Fish when the damaged player's top card is not a permanent")
    void createsFishForNonPermanentTopCard() {
        Permanent gear = addReadyGear();
        Permanent creature = addReadyCreature();
        gear.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        Shock topCard = new Shock();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(findPermanents(player1, "Fish")).hasSize(1);
    }

    private Permanent addReadyGear() {
        Permanent gear = harness.addToBattlefieldAndReturn(player1, new FishingGear());
        gear.setSummoningSick(false);
        return gear;
    }

    private Permanent addReadyCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private Permanent findPermanent(com.github.laxika.magicalvibes.model.Player player, Card card) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElse(null);
    }
}
