package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.RedcapThief;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CharmingScoundrel.class, RedcapThief.class, Plains.class})
class CharmingScoundrelTest extends BaseCardTest {

    @Test
    void choosesModeWhenEnterTriggerIsPutOnStack() {
        harness.setHand(player1, List.of(new CharmingScoundrel()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.context()).isInstanceOf(ChoiceContext.TriggeredModalChoice.class);
    }

    @Test
    void rummageModeDiscardsThenDraws() {
        Plains discarded = new Plains();
        RedcapThief drawn = new RedcapThief();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new CharmingScoundrel(), discarded));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Discard a card, then draw a card");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Plains");
        harness.assertInHand(player1, "Redcap Thief");
    }

    @Test
    void treasureModeCreatesTreasureToken() {
        harness.setHand(player1, List.of(new CharmingScoundrel()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Create a Treasure token");
        harness.passBothPriorities();

        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.getCard().isToken()).isTrue();
        assertThat(treasure.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
    }

    @Test
    void wickedRoleModeAttachesRoleAndBoostsTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RedcapThief());
        harness.setHand(player1, List.of(new CharmingScoundrel()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Create a Wicked Role token attached to target creature you control");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent role = findPermanent(player1, "Wicked");
        assertThat(role.getCard().isToken()).isTrue();
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
    }

    @Test
    void wickedRoleModeCannotTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RedcapThief());
        harness.setHand(player1, List.of(new CharmingScoundrel()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Create a Wicked Role token attached to target creature you control");
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rummageModeStillDrawsWithAnEmptyHand() {
        harness.setLibrary(player1, List.of(new Plains()));
        harness.setHand(player1, List.of(new CharmingScoundrel()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Discard a card, then draw a card");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Plains");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void replacingWickedRoleDrainsOpponentAndKeepsOnlyNewestRole() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RedcapThief());
        harness.setHand(player1, List.of(new CharmingScoundrel(), new CharmingScoundrel()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Create a Wicked Role token attached to target creature you control");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        Permanent firstRole = findPermanent(player1, "Wicked");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Create a Wicked Role token attached to target creature you control");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getSubtypes().contains(CardSubtype.ROLE))
                .singleElement().satisfies(role -> {
                    assertThat(role.getId()).isNotEqualTo(firstRole.getId());
                    assertThat(role.getAttachedTo()).isEqualTo(target.getId());
                });
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }
}
