package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HonestWork;
import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.cards.n.NyxbornSeaguard;
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

@CardUsed({EchoingCalm.class, AngelicChorus.class, GloriousAnthem.class, GrizzlyBears.class})
class EchoingCalmTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target enchantment and every other enchantment with the same name")
    void destroysTargetAndAllWithSameName() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.addToBattlefield(player1, new AngelicChorus());

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.setHand(player1, List.of(new EchoingCalm()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Angelic Chorus");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Leaves enchantments with different names on the battlefield")
    void leavesDifferentNames() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.addToBattlefield(player2, new GloriousAnthem());

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.setHand(player1, List.of(new EchoingCalm()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Cannot target a non-enchantment permanent")
    void cannotTargetNonEnchantment() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EchoingCalm()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchantment");
    }

    @Test
    @DisplayName("Can target an enchantment controlled by the caster")
    void canTargetOwnEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AngelicChorus());
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new EchoingCalm()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Angelic Chorus");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player1, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Same-name enchantment cards in hand and graveyard are unaffected")
    void affectsOnlyBattlefieldEnchantments() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());
        AngelicChorus inGraveyard = new AngelicChorus();
        harness.setGraveyard(player1, List.of(inGraveyard));
        harness.setHand(player1, List.of(new EchoingCalm(), new AngelicChorus()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInHand(player1, "Angelic Chorus");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(inGraveyard);
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @CardUsed({Disenchant.class})
    @DisplayName("Leaves other same-name enchantments alone when the target leaves before resolution")
    void doesNothingWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.setHand(player1, List.of(new EchoingCalm(), new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertOnBattlefield(player1, "Angelic Chorus");
        harness.assertInGraveyard(player1, "Echoing Calm");
        harness.assertInGraveyard(player1, "Disenchant");
    }

    @Test
    @CardUsed({HonestWork.class, NyxbornCourser.class})
    @DisplayName("An enchantment with the target's former name survives after the target is renamed")
    void doesNotMatchFormerNameOfRenamedTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        Permanent unchanged = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HonestWork());
        aura.setAttachedTo(target.getId());
        harness.setHand(player1, List.of(new EchoingCalm()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(unchanged).doesNotContain(target);
        harness.assertInGraveyard(player2, "Nyxborn Courser");
        harness.assertInGraveyard(player1, "Honest Work");
    }

    @Test
    @CardUsed({HonestWork.class, NyxbornCourser.class, NyxbornSeaguard.class})
    @DisplayName("Destroys differently printed enchantments that currently have the same name")
    void matchesCurrentNamesOfRenamedEnchantments() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new NyxbornSeaguard());
        Permanent firstAura = harness.addToBattlefieldAndReturn(player1, new HonestWork());
        firstAura.setAttachedTo(target.getId());
        Permanent secondAura = harness.addToBattlefieldAndReturn(player1, new HonestWork());
        secondAura.setAttachedTo(other.getId());
        harness.setHand(player1, List.of(new EchoingCalm()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target, other);
        harness.assertInGraveyard(player2, "Nyxborn Courser");
        harness.assertInGraveyard(player2, "Nyxborn Seaguard");
        harness.assertNotOnBattlefield(player1, "Honest Work");
    }
}
