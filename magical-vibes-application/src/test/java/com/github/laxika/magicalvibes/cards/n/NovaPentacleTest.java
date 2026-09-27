package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.ChainLightning;
import com.github.laxika.magicalvibes.cards.p.PsionicEntity;
import com.github.laxika.magicalvibes.cards.w.WallOfEarth;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({NovaPentacle.class, ChainLightning.class, PsionicEntity.class, WallOfEarth.class})
class NovaPentacleTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent chooses the target creature during activation")
    void opponentChoosesTargetCreature() {
        Permanent pentacle = addReadyPermanent(player1, new NovaPentacle());
        Permanent ownCreature = addCreatureReady(player1, new WallOfEarth());
        Permanent opponentCreature = addCreatureReady(player2, new WallOfEarth());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(player1, pentacle), null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validPermanentIds()).contains(ownCreature.getId(), opponentCreature.getId());

        harness.handlePermanentChosen(player2, ownCreature.getId());

        assertThat(pentacle.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Redirects the next damage from the chosen source to the creature chosen by the opponent")
    void redirectsNextDamageToOpponentChosenCreature() {
        Permanent pentacle = addReadyPermanent(player1, new NovaPentacle());
        Permanent source = addCreatureReady(player1, new PsionicEntity());
        Permanent target = addCreatureReady(player1, new WallOfEarth());
        int lifeBefore = gd.getLife(player1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(player1, pentacle), null, null);
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        harness.activateAbility(player1, indexOf(player1, source), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can choose a damage-dealing spell on the stack as the source")
    void canChooseDamageDealingSpellOnStackAsSource() {
        Permanent pentacle = addReadyPermanent(player1, new NovaPentacle());
        Permanent target = addCreatureReady(player1, new WallOfEarth());
        ChainLightning chainLightning = new ChainLightning();

        harness.setHand(player1, List.of(chainLightning));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, player1.getId());
        harness.activateAbility(player1, indexOf(player1, pentacle), null, null);
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice sourceChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(sourceChoice).isNotNull();
        assertThat(sourceChoice.playerId()).isEqualTo(player1.getId());
        assertThat(sourceChoice.validIds()).contains(chainLightning.getId());

        harness.handlePermanentChosen(player1, chainLightning.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
