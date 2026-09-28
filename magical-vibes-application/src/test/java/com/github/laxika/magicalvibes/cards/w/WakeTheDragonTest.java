package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WakeTheDragon.class, LeoninScimitar.class, GrizzlyBears.class})
class WakeTheDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Wake the Dragon creates a 6/6 black and red Dragon with flying and menace")
    void createsDragonToken() {
        Permanent dragon = castWakeTheDragon();

        assertThat(dragon.getCard().getPower()).isEqualTo(6);
        assertThat(dragon.getCard().getToughness()).isEqualTo(6);
        assertThat(dragon.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(dragon.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED);
        assertThat(dragon.getCard().hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(dragon.getCard().hasKeyword(Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Dragon combat damage lets its controller gain control of an artifact the damaged player controls")
    void combatDamageStealsDamagedPlayersArtifact() {
        Permanent dragon = castWakeTheDragon();
        dragon.setSummoningSick(false);
        dragon.setAttacking(true);
        Permanent scimitar = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, scimitar.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentController(gd, scimitar.getId())).isEqualTo(player1.getId());
        assertThat(gqs.findPermanentController(gd, bears.getId())).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Flashback creates the Dragon and exiles Wake the Dragon")
    void flashbackCreatesDragonAndExilesCard() {
        harness.setGraveyard(player1, List.of(new WakeTheDragon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Dragon")).hasSize(1);
        harness.assertNotInGraveyard(player1, "Wake the Dragon");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Wake the Dragon"));
    }

    private Permanent castWakeTheDragon() {
        harness.setHand(player1, List.of(new WakeTheDragon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Dragon");
    }
}
