package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChainLightning;
import com.github.laxika.magicalvibes.cards.c.Cleanse;
import com.github.laxika.magicalvibes.cards.d.DAvenantArcher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Backdraft.class, ChainLightning.class, Cleanse.class, DAvenantArcher.class})
class BackdraftTest extends BaseCardTest {

    @Test
    void dealsHalfTheDamageDealtByOneSorceryRoundedDown() {
        harness.setHand(player1, List.of(new ChainLightning()));
        harness.setHand(player2, List.of(new Backdraft()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0);
        harness.handlePermanentChosen(player2, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    void choosesAnEligiblePlayerWhenItResolves() {
        harness.setHand(player1, List.of(new ChainLightning()));
        harness.setHand(player2, List.of(new Backdraft()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPlayerIds()).containsExactly(player1.getId());

        harness.handlePermanentChosen(player2, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    void aSorceryNeedNotHaveDealtDamageToQualify() {
        harness.setHand(player1, List.of(new Cleanse()));
        harness.setHand(player2, List.of(new Backdraft()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0);
        harness.handlePermanentChosen(player2, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void resolvesWithoutDamageWhenNoPlayerCastASorcery() {
        harness.setHand(player1, List.of(new Backdraft()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Backdraft");
    }

    @Test
    void canChooseItsOwnController() {
        harness.setHand(player1, List.of(new ChainLightning(), new Backdraft()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.castAndResolveInstant(player1, 0);
        harness.handlePermanentChosen(player1, player1.getId());

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 17);
    }

    @Test
    void countsDamageDealtToACreatureIncludingExcessDamage() {
        var creature = harness.addToBattlefieldAndReturn(player2, new DAvenantArcher());
        harness.setHand(player1, List.of(new ChainLightning()));
        harness.setHand(player2, List.of(new Backdraft()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, creature.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0);
        harness.handlePermanentChosen(player2, player1.getId());

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    void controllerChoosesWhichSorceryDeterminesDamage() {
        Cleanse cleanse = new Cleanse();
        harness.setHand(player1, List.of(new ChainLightning(), cleanse));
        harness.setHand(player2, List.of(new Backdraft()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0);
        harness.handlePermanentChosen(player2, player1.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player2, cleanse.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Backdraft");
    }
}
