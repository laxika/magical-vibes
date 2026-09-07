package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagusOfTheScroll.class, GrizzlyBears.class})
class MagusOfTheScrollTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability prompts the controller to name a card")
    void resolvingPromptsController() {
        addReadyMagus(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(createNamedCard("Lightning Bolt")));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        var interaction = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(interaction.playerId()).isEqualTo(player1.getId());
        assertThat(interaction.context())
                .isInstanceOf(ChoiceContext.ChooseNameRevealRandomHandCardDamageChoice.class);
    }

    @Test
    @DisplayName("A matching reveal deals 2 damage to the target player")
    void matchingRevealDealsDamageToPlayer() {
        harness.setLife(player2, 20);
        addReadyMagus(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(createNamedCard("Lightning Bolt")));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lightning Bolt");

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A nonmatching reveal deals no damage")
    void mismatchedRevealDealsNoDamage() {
        harness.setLife(player2, 20);
        addReadyMagus(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(createNamedCard("Grizzly Bears")));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lightning Bolt");

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A matching reveal can deal 2 damage to a target creature")
    void matchingRevealDamagesTargetCreature() {
        addReadyMagus(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(createNamedCard("Lightning Bolt")));
        Permanent bears = harness.addToBattlefieldAndReturn(player2,
                new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lightning Bolt");

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(bears.getId()));
    }

    private Permanent addReadyMagus(Player player) {
        Permanent perm = new Permanent(new MagusOfTheScroll());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private static Card createNamedCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}");
        card.setColor(CardColor.RED);
        return card;
    }
}
