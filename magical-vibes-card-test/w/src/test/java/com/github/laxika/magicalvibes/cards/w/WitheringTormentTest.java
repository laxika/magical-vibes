package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.cards.p.PatchworkBeastie;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WitheringTorment.class, AngelicChorus.class, GrizzlyBears.class, Forest.class,
        PatchworkBeastie.class, LeylineOfTheVoid.class})
class WitheringTormentTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature and the spell's controller loses 2 life")
    void destroysCreatureAndLosesLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castWitheringTorment(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Destroys an enchantment and the spell's controller loses 2 life")
    void destroysEnchantmentAndLosesLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());

        castWitheringTorment(target);

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new WitheringTorment()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or enchantment");
    }

    @Test
    @DisplayName("Fizzles without life loss when the target leaves before resolution")
    void fizzlesWithoutLifeLossWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WitheringTorment()));
        addMana();
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can destroy your own artifact creature")
    void destroysOwnArtifactCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PatchworkBeastie());

        castWitheringTorment(target);

        harness.assertNotOnBattlefield(player1, "Patchwork Beastie");
        harness.assertInGraveyard(player1, "Patchwork Beastie");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Still loses life when an indestructible creature survives")
    void losesLifeEvenWhenTargetIsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PatchworkBeastie());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        castWitheringTorment(target);

        harness.assertOnBattlefield(player2, "Patchwork Beastie");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Still loses life when destruction sends the target to exile")
    void losesLifeWhenDestroyedTargetIsExiled() {
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PatchworkBeastie());

        castWitheringTorment(target);

        harness.assertNotOnBattlefield(player2, "Patchwork Beastie");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Patchwork Beastie"));
        harness.assertNotInGraveyard(player2, "Patchwork Beastie");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    private void castWitheringTorment(Permanent target) {
        harness.setHand(player1, List.of(new WitheringTorment()));
        addMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
