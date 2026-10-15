package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.ApexObservatory;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EyeOfOjerTaq.class, ApexObservatory.class, GrizzlyBears.class, Shock.class, Naturalize.class})
class EyeOfOjerTaqTest extends BaseCardTest {

    @Test
    @DisplayName("Crafting with two creatures transforms and restricts the chosen type")
    void craftsWithTwoSharingCardType() {
        harness.addToBattlefield(player1, new EyeOfOjerTaq());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("CREATURE");
        harness.handleListChoice(player1, "CREATURE");

        Permanent observatory = findPermanent(player1, "Apex Observatory");
        assertThat(observatory.isTapped()).isTrue();
        harness.performUntapStep(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("Crafting rejects materials without a shared card type")
    void rejectsMaterialsWithoutSharedCardType() {
        harness.addToBattlefield(player1, new EyeOfOjerTaq());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("share a card type");
    }

    @Test
    void manaAbilityProducesChosenColor() {
        harness.addToBattlefield(player1, new EyeOfOjerTaq());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(findPermanent(player1, "Eye of Ojer Taq").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cannotCraftWhileSpellIsOnStack() {
        harness.addToBattlefield(player1, new EyeOfOjerTaq());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Eye of Ojer Taq");
    }

    @Test
    void craftsUsingBattlefieldAndGraveyardMaterials() {
        harness.addToBattlefield(player1, new EyeOfOjerTaq());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "CREATURE");

        harness.assertOnBattlefield(player1, "Apex Observatory");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards.stream().filter(entry -> entry.card() instanceof GrizzlyBears).count())
                .isEqualTo(2);
    }

    @Test
    void freeCastAppliesOnlyToNextSpellOfChosenType() {
        craftWithCreaturesAndUntap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock(), new GrizzlyBears(), new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void abilityUsesChosenTypeWhenObservatoryIsDestroyedInResponse() {
        craftWithCreaturesAndUntap();
        Permanent observatory = findPermanent(player1, "Apex Observatory");
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castInstant(player2, 0, observatory.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Apex Observatory");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private void craftWithCreaturesAndUntap() {
        harness.addToBattlefield(player1, new EyeOfOjerTaq());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "CREATURE");
        harness.performUntapStep(player1);
    }
}
