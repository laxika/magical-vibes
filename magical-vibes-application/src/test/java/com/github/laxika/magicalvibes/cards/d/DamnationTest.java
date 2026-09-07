package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TrollAscetic;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Damnation.class, FountainOfYouth.class, GrizzlyBears.class, TrollAscetic.class, WallOfWood.class})
class DamnationTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures and leaves noncreature permanents alone")
    void destroysAllCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new WallOfWood());
        harness.addToBattlefield(player1, new FountainOfYouth());
        castDamnation();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Wall of Wood");
        harness.assertOnBattlefield(player1, "Fountain of Youth");
    }

    @Test
    @DisplayName("Creatures cannot regenerate from Damnation")
    void creaturesCannotRegenerate() {
        Permanent troll = new Permanent(new TrollAscetic());
        troll.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(troll);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        assertThat(troll.getRegenerationShield()).isEqualTo(1);

        castDamnation();

        harness.assertNotOnBattlefield(player2, "Troll Ascetic");
        harness.assertInGraveyard(player2, "Troll Ascetic");
    }

    private void castDamnation() {
        harness.setHand(player1, List.of(new Damnation()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
