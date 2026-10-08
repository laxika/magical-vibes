package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BastionMastodon;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WeldingSparks.class, FountainOfYouth.class, GiantSpider.class, BastionMastodon.class})
class WeldingSparksTest extends BaseCardTest {

    @Test
    @DisplayName("Deals three damage when its controller controls no artifacts")
    void dealsBaseDamage() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        cast(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Deals one additional damage for each artifact its controller controls")
    void countsArtifactsControlledBySpellController() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        cast(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Counts artifacts when the spell resolves")
    void countsArtifactsAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new WeldingSparks()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new WeldingSparks()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counts an artifact creature even when it is the targeted own creature")
    void countsOwnArtifactCreatureTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BastionMastodon());

        cast(target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("Two controlled artifacts increase damage to five")
    void countsMultipleArtifacts() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new FountainOfYouth());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BastionMastodon());

        cast(target);

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Bastion Mastodon");
    }

    @Test
    @DisplayName("An artifact that leaves before resolution does not increase damage")
    void excludesArtifactsThatLeaveBeforeResolution() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new WeldingSparks()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        gd.playerGraveyards.get(player1.getId()).add(artifact.getCard());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new WeldingSparks()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new WeldingSparks()));
        addMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
