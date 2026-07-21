package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DunesOfTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C} produces one colorless mana")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new DunesOfTheDead());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("When destroyed, creates a 2/2 black Zombie token under its controller")
    void destroyedCreatesZombieToken() {
        harness.addToBattlefield(player1, new DunesOfTheDead());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player1, "Dunes of the Dead");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities(); // Resolve Stone Rain — land to graveyard

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dunes of the Dead"));
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // Resolve graveyard trigger

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .toList();
        assertThat(tokens).hasSize(1);

        Permanent zombie = tokens.getFirst();
        assertThat(zombie.getCard().getPower()).isEqualTo(2);
        assertThat(zombie.getCard().getToughness()).isEqualTo(2);
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(zombie.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Graveyard trigger creates the Zombie under the land's controller")
    void triggerBelongsToController() {
        harness.addToBattlefield(player2, new DunesOfTheDead());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Dunes of the Dead");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities(); // Resolve Stone Rain
        harness.passBothPriorities(); // Resolve graveyard trigger

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .toList()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .toList()).isEmpty();
    }
}
