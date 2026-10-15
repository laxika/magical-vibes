package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AssaultFormation;
import com.github.laxika.magicalvibes.cards.a.AncestralStatue;
import com.github.laxika.magicalvibes.cards.t.TerritorialRoc;
import com.github.laxika.magicalvibes.cards.s.SpidersilkNet;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FateForgotten.class, SpidersilkNet.class, AssaultFormation.class, TerritorialRoc.class, Forest.class,
        AncestralStatue.class})
class FateForgottenTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target artifact")
    void exilesArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpidersilkNet());

        castFateForgotten(target);

        harness.assertNotOnBattlefield(player2, "Spidersilk Net");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Exiles a target enchantment")
    void exilesEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AssaultFormation());

        castFateForgotten(target);

        harness.assertNotOnBattlefield(player2, "Assault Formation");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Rejects a creature target")
    void rejectsCreatureTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TerritorialRoc());
        harness.setHand(player1, List.of(new FateForgotten()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    @Test
    @DisplayName("Rejects a land target")
    void rejectsLandTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new FateForgotten()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    @Test
    @DisplayName("Can exile its controller's own artifact")
    void exilesOwnArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SpidersilkNet());

        castFateForgotten(target);

        harness.assertNotOnBattlefield(player1, "Spidersilk Net");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard());
        harness.assertNotInGraveyard(player1, "Spidersilk Net");
    }

    @Test
    @DisplayName("Can exile an artifact that is also a creature")
    void exilesArtifactCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncestralStatue());

        castFateForgotten(target);

        harness.assertNotOnBattlefield(player2, "Ancestral Statue");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        harness.assertNotInGraveyard(player2, "Ancestral Statue");
    }

    @Test
    @DisplayName("Does not exile a different artifact when its target leaves before resolution")
    void targetLeavingBattlefieldMakesSpellFizzle() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpidersilkNet());
        harness.addToBattlefield(player2, new AncestralStatue());
        harness.setHand(player1, List.of(new FateForgotten()));
        addMana();
        harness.castInstant(player1, 0, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, target));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ancestral Statue");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target.getCard());
        harness.assertInGraveyard(player1, "Fate Forgotten");
        assertThat(gd.stack).isEmpty();
    }

    private void castFateForgotten(Permanent target) {
        harness.setHand(player1, List.of(new FateForgotten()));
        addMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
