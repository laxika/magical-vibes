package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KalemnesCaptain.class, Bonesplitter.class, GloriousAnthem.class, GrizzlyBears.class})
class KalemnesCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("When Kalemne's Captain becomes monstrous, it exiles all artifacts and enchantments")
    void becomingMonstrousExilesArtifactsAndEnchantments() {
        Permanent captain = addReadyCaptain();
        harness.addToBattlefield(player1, new Bonesplitter());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new GrizzlyBears());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(captain.isMonstrous()).isTrue();
        harness.assertNotOnBattlefield(player1, "Bonesplitter");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Monstrosity can be activated again but does nothing once the Captain is monstrous")
    void activatingMonstrosityAgainDoesNothing() {
        Permanent captain = addReadyCaptain();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.addToBattlefield(player2, new Bonesplitter());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(captain.isMonstrous()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Bonesplitter");
    }

    @Test
    @DisplayName("The exile trigger resolves separately and includes permanents added after monstrosity resolves")
    void exileTriggerChecksPermanentsWhenItResolves() {
        Permanent captain = addReadyCaptain();
        Bonesplitter equipment = new Bonesplitter();
        harness.addToBattlefield(player1, equipment);
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(captain.isMonstrous()).isTrue();
        harness.assertOnBattlefield(player1, "Bonesplitter");
        assertThat(gd.findExiledCard(equipment.getId())).isNull();
        GloriousAnthem anthem = new GloriousAnthem();
        harness.addToBattlefield(player2, anthem);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(equipment.getId())).isNotNull();
        assertThat(gd.findExiledCard(anthem.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Bonesplitter");
        harness.assertNotInGraveyard(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player1, "Kalemne's Captain");
    }

    @Test
    @DisplayName("Multiple pending monstrosity activations add counters and trigger exile only once")
    void multiplePendingActivationsBecomeMonstrousOnlyOnce() {
        Permanent captain = addReadyCaptain();
        addMonstrosityMana();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.addToBattlefield(player2, new Bonesplitter());
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(captain.isMonstrous()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Bonesplitter");
    }

    @Test
    @DisplayName("Monstrosity is usable while summoning sick and tapped")
    void monstrosityDoesNotRequireAnUntappedReadyCreature() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new KalemnesCaptain());
        captain.setSummoningSick(true);
        captain.tap();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(captain.isMonstrous()).isTrue();
        assertThat(captain.isTapped()).isTrue();
    }

    private Permanent addReadyCaptain() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new KalemnesCaptain());
        captain.setSummoningSick(false);
        return captain;
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
