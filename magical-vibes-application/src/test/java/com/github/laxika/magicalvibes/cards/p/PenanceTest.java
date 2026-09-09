package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.Grollub;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.SonicBurst;
import com.github.laxika.magicalvibes.cards.w.WoodElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Penance.class, WoodElves.class, Grollub.class, RagingGoblin.class, SonicBurst.class})
class PenanceTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a card from hand on top of the library and prevents the next damage from a chosen black source")
    void preventsDamageFromChosenBlackSource() {
        harness.setLife(player1, 20);
        addPenance(player1);
        Card chosenCard = new WoodElves();
        harness.setHand(player1, List.of(chosenCard));
        harness.setLibrary(player1, List.of(new WoodElves()));
        Permanent zombie = addReady(player2, new Grollub());

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, zombie.getId());

        zombie.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(chosenCard);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents damage from a chosen red source")
    void preventsDamageFromChosenRedSource() {
        harness.setLife(player1, 20);
        addPenance(player1);
        harness.setHand(player1, List.of(new WoodElves()));
        Permanent giant = addReady(player2, new RagingGoblin());

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, giant.getId());

        giant.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("A source that is neither black nor red cannot be chosen")
    void nonBlackOrRedSourceCannotBeChosen() {
        addPenance(player1);
        harness.setHand(player1, List.of(new WoodElves()));
        addReady(player2, new WoodElves());

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("No permanents on the battlefield"));
    }

    @Test
    @DisplayName("Prevents the chosen source's next damage to a creature you control")
    void preventsDamageToCreatureYouControl() {
        addPenance(player1);
        harness.setHand(player1, List.of(new WoodElves()));
        Permanent blocker = addReady(player1, new WoodElves());
        Permanent attacker = addReady(player2, new RagingGoblin());

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Can choose a red spell on the stack as the source")
    void preventsDamageFromRedSpellOnStack() {
        harness.setLife(player1, 20);
        addPenance(player1);
        SonicBurst sonicBurst = new SonicBurst();
        harness.setHand(player2, List.of(sonicBurst, new WoodElves()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new WoodElves()));
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(sonicBurst.getId());
        harness.handlePermanentChosen(player1, sonicBurst.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    private Permanent addPenance(Player player) {
        Permanent permanent = new Permanent(new Penance());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
