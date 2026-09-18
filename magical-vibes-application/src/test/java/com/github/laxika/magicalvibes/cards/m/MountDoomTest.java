package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MountDoom.class, GrizzlyBears.class, LlanowarElves.class})
class MountDoomTest extends BaseCardTest {

    @Test
    void addsBlackOrRedManaAndCostsOneLife() {
        Permanent mountDoom = addReadyMountDoom(player1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(mountDoom.isTapped()).isTrue();
    }

    @Test
    void dealsOneDamageToEachOpponent() {
        addReadyMountDoom(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void sacrificesSourceAndLegendaryArtifactThenKeepsUpToTwoCreatures() {
        Permanent mountDoom = addReadyMountDoom(player1);
        Card legendaryArtifact = legendaryArtifact();
        harness.addToBattlefield(player1, legendaryArtifact);
        Permanent firstKept = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondKept = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.DestroyRestChoice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(firstKept.getId(), secondKept.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstKept);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(secondKept);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Llanowar Elves"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mountDoom);
        harness.assertInGraveyard(player1, "Mount Doom");
        harness.assertInGraveyard(player1, "Legendary artifact");
    }

    @Test
    void cannotActivateDestructionAbilityWithoutLegendaryArtifact() {
        addReadyMountDoom(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyMountDoom(Player player) {
        Permanent permanent = new Permanent(new MountDoom());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private static Card legendaryArtifact() {
        Card card = new Card();
        card.setName("Legendary artifact");
        card.setType(CardType.ARTIFACT);
        card.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        return card;
    }
}
