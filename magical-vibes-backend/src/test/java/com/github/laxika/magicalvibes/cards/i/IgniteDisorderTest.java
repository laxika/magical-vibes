package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.l.LightningElemental;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.s.SamiteHealer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IgniteDisorderTest extends BaseCardTest {

    @Test
    void deals3DamageToSingleWhiteCreature() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new IgniteDisorder()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent target = addToBattlefield(player2, new SoulWarden());

        harness.castInstant(player1, 0, Map.of(target.getId(), 3));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // SoulWarden is 1/1, 3 damage kills it
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Soul Warden"));
    }

    @Test
    void divides2And1DamageAmongTwoWhiteCreatures() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new IgniteDisorder()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent target1 = addToBattlefield(player2, new SoulWarden());
        Permanent target2 = addToBattlefield(player2, new SamiteHealer());

        harness.castInstant(player1, 0, Map.of(target1.getId(), 2, target2.getId(), 1));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Both are 1/1, both die
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target1.getId()))
                .noneMatch(p -> p.getId().equals(target2.getId()));
    }

    @Test
    void divides1DamageAmongThreeCreatures() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new IgniteDisorder()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent white1 = addToBattlefield(player2, new SoulWarden());
        Permanent white2 = addToBattlefield(player2, new SamiteHealer());
        Permanent blue1 = addToBattlefield(player2, new FugitiveWizard());

        harness.castInstant(player1, 0, Map.of(
                white1.getId(), 1,
                white2.getId(), 1,
                blue1.getId(), 1
        ));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // All three are 1/1, all die
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(white1.getId()))
                .noneMatch(p -> p.getId().equals(white2.getId()))
                .noneMatch(p -> p.getId().equals(blue1.getId()));
    }

    @Test
    void canTargetBlueCreature() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new IgniteDisorder()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent blue = addToBattlefield(player2, new FugitiveWizard());

        harness.castInstant(player1, 0, Map.of(blue.getId(), 3));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(blue.getId()));
    }

    @Test
    void cannotTargetRedCreature() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new IgniteDisorder()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent red = addToBattlefield(player2, new LightningElemental());

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(red.getId(), 3))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageAssignmentsMustSumTo3() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new IgniteDisorder()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent target = addToBattlefield(player2, new SoulWarden());

        // Only assigning 2 damage — should fail
        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(target.getId(), 2))
        ).isInstanceOf(IllegalStateException.class);
    }

    private Permanent addToBattlefield(Player player, Card card) {
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
