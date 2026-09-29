package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AceFearlessRebel.class, GrizzlyBears.class, Ornithopter.class})
class AceFearlessRebelTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact puts a counter on Ace and makes it fight")
    void sacrificingArtifactPutsCounterAndFights() {
        Permanent ace = addReadyPermanent(player1, new AceFearlessRebel());
        Permanent artifact = addReadyPermanent(player1, new Ornithopter());
        Permanent defender = addReadyPermanent(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, defender.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ace)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ace)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(defender.getId()));
    }

    @Test
    @DisplayName("Declining Nitro-9 keeps the artifact and Ace unchanged")
    void decliningSacrificeDoesNothing() {
        Permanent ace = addReadyPermanent(player1, new AceFearlessRebel());
        Permanent artifact = addReadyPermanent(player1, new Ornithopter());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, ace)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ace)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
    }

    @Test
    @DisplayName("Sacrificing an artifact still adds a counter when there is no fight target")
    void sacrificeSucceedsWithoutFightTarget() {
        Permanent ace = addReadyPermanent(player1, new AceFearlessRebel());
        Permanent artifact = addReadyPermanent(player1, new Ornithopter());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ace)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ace)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
