package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.z.ZoeticCavern;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaelstromPulse.class, GrizzlyBears.class, HillGiant.class, Island.class,
        Unsummon.class, ZoeticCavern.class, HowlingMine.class, GloriousAnthem.class})
class MaelstromPulseTest extends BaseCardTest {

    private void giveManaAndCard() {
        harness.setHand(player1, List.of(new MaelstromPulse()));
        harness.addMana(player1, ManaColor.BLACK, 2); // {B} + {1}
        harness.addMana(player1, ManaColor.GREEN, 1); // {G}
    }

    @Test
    @DisplayName("Destroys target nonland permanent and every other permanent with the same name")
    void destroysTargetAndAllWithSameName() {
        // Two copies under the opponent, one under the caster — all share a name.
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        // A differently-named permanent that must survive.
        harness.addToBattlefield(player2, new HillGiant());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        giveManaAndCard();

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Destroys only the target when no other permanent shares its name")
    void destroysLoneTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        giveManaAndCard();

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // valid target so the spell is playable
        harness.addToBattlefield(player2, new Island());
        UUID landId = harness.getPermanentId(player2, "Island");
        giveManaAndCard();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    @DisplayName("Destroys matching artifacts while leaving differently named enchantments")
    void destroysMatchingArtifacts() {
        harness.addToBattlefield(player1, new HowlingMine());
        harness.addToBattlefield(player2, new HowlingMine());
        harness.addToBattlefield(player2, new GloriousAnthem());
        UUID targetId = harness.getPermanentId(player2, "Howling Mine");
        giveManaAndCard();

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertInGraveyard(player1, "Howling Mine");
        harness.assertInGraveyard(player2, "Howling Mine");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Destroys matching enchantments")
    void destroysMatchingEnchantments() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player2, new GloriousAnthem());
        UUID targetId = harness.getPermanentId(player2, "Glorious Anthem");
        giveManaAndCard();

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertInGraveyard(player1, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Does not destroy other copies when the target leaves the battlefield")
    void doesNothingWhenTargetLeavesBattlefield() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        giveManaAndCard();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Maelstrom Pulse");
    }

    @Test
    @DisplayName("A nameless face-down target does not share its name with other permanents")
    void destroysOnlyTargetWhenFaceDown() {
        harness.setHand(player1, List.of(new ZoeticCavern(), new ZoeticCavern()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        UUID targetId = gd.playerBattlefields.get(player1.getId()).getFirst().getId();
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        UUID otherId = gd.playerBattlefields.get(player1.getId()).getLast().getId();
        harness.addToBattlefield(player2, new ZoeticCavern());
        giveManaAndCard();

        harness.castAndResolveSorcery(player1, 0, targetId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId).containsExactly(otherId);
        harness.assertOnBattlefield(player2, "Zoetic Cavern");
        harness.assertInGraveyard(player1, "Zoetic Cavern");
    }
}
