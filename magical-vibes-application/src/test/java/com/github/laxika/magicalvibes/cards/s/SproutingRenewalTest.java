package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KraulForagers;
import com.github.laxika.magicalvibes.cards.l.LedevGuardian;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({SproutingRenewal.class, AngelicChorus.class, FountainOfYouth.class,
        GrizzlyBears.class, KraulForagers.class, LedevGuardian.class, SelesnyaLocket.class})
class SproutingRenewalTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 1 creates a 2/2 green and white Elf Knight token with vigilance")
    void createsElfKnightToken() {
        harness.setHand(player1, List.of(new SproutingRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent token = findPermanent(player1, "Elf Knight");
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ELF, CardSubtype.KNIGHT);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Mode 2 destroys a target artifact")
    void destroysArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new SproutingRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 1, artifact.getId());

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Mode 2 destroys a target enchantment")
    void destroysEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new SproutingRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 1, enchantment.getId());

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Mode 2 cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SproutingRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Convoke pays the entire cost using a green creature and two white creatures, even with summoning sickness")
    void convokesEntireCostWithSummoningSickCreatures() {
        Permanent green = harness.addToBattlefieldAndReturn(player1, new KraulForagers());
        Permanent white1 = harness.addToBattlefieldAndReturn(player1, new LedevGuardian());
        Permanent white2 = harness.addToBattlefieldAndReturn(player1, new LedevGuardian());
        green.setSummoningSick(true);
        white1.setSummoningSick(true);
        white2.setSummoningSick(true);
        harness.setHand(player1, List.of(new SproutingRenewal()));

        harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(green.getId(), white1.getId(), white2.getId()));
        harness.passBothPriorities();

        assertThat(green.isTapped()).isTrue();
        assertThat(white1.isTapped()).isTrue();
        assertThat(white2.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Elf Knight")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Sprouting Renewal");
    }

    @Test
    @DisplayName("White creatures cannot convoke the green part of the cost")
    void cannotConvokeGreenCostWithWhiteCreatures() {
        Permanent white1 = harness.addToBattlefieldAndReturn(player1, new LedevGuardian());
        Permanent white2 = harness.addToBattlefieldAndReturn(player1, new LedevGuardian());
        Permanent white3 = harness.addToBattlefieldAndReturn(player1, new LedevGuardian());
        harness.setHand(player1, List.of(new SproutingRenewal()));

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(white1.getId(), white2.getId(), white3.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Elf Knight")).isZero();
    }

    @Test
    @DisplayName("The destruction mode can target your own artifact without creating a token")
    void destroysOwnArtifactWithoutCreatingToken() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SelesnyaLocket());
        harness.setHand(player1, List.of(new SproutingRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 1, artifact.getId());

        harness.assertNotOnBattlefield(player1, "Selesnya Locket");
        harness.assertInGraveyard(player1, "Selesnya Locket");
        assertThat(countPermanents(player1, "Elf Knight")).isZero();
    }

    @Test
    @DisplayName("An absent destruction target does not cause the spell to create a token instead")
    void doesNotSwitchModesWhenTargetLeavesBattlefield() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SelesnyaLocket());
        harness.setHand(player1, List.of(new SproutingRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, 1, artifact.getId());
        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        gd.playerGraveyards.get(player2.getId()).add(artifact.getCard());

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elf Knight")).isZero();
        harness.assertInGraveyard(player1, "Sprouting Renewal");
    }
}
