package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.p.Psychomancer;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.cards.w.WirewoodElf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EverythingComesToDust.class, ElvishWarrior.class, WirewoodElf.class,
        GlorySeeker.class, DarksteelRelic.class, GloriousAnthem.class,
        HolyStrength.class, Psychomancer.class, SwordsToPlowshares.class})
class EverythingComesToDustTest extends BaseCardTest {

    @Test
    void sparesCreaturesSharingATypeWithAConvokingCreatureAndExilesOtherPermanentTypes() {
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.addToBattlefield(player2, new WirewoodElf());
        harness.addToBattlefield(player2, new GlorySeeker());
        harness.addToBattlefield(player1, new DarksteelRelic());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new EverythingComesToDust()));
        harness.addMana(player1, ManaColor.WHITE, 9);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(convoker.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elvish Warrior");
        harness.assertOnBattlefield(player2, "Wirewood Elf");
        harness.assertNotOnBattlefield(player2, "Glory Seeker");
        harness.assertNotOnBattlefield(player1, "Darksteel Relic");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    void withoutConvokeExilesAllCreatures() {
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.addToBattlefield(player2, new WirewoodElf());
        harness.setHand(player1, List.of(new EverythingComesToDust()));
        harness.addMana(player1, ManaColor.WHITE, 10);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Elvish Warrior");
        harness.assertNotOnBattlefield(player2, "Wirewood Elf");
    }

    @Test
    void usesLastKnownCreatureTypesWhenTheConvokerIsExiledInResponse() {
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.addToBattlefield(player2, new WirewoodElf());
        harness.addToBattlefield(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new EverythingComesToDust()));
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 9);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(convoker.getId()));
        harness.castInstant(player2, 0, convoker.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Elvish Warrior");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wirewood Elf");
        harness.assertNotOnBattlefield(player2, "Glory Seeker");
    }

    @Test
    void preservesCreaturesSharingAnyTypeWithEitherConvoker() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        harness.addToBattlefield(player2, new WirewoodElf());
        harness.addToBattlefield(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new EverythingComesToDust()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(elf.getId(), human.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elvish Warrior");
        harness.assertOnBattlefield(player1, "Glory Seeker");
        harness.assertOnBattlefield(player2, "Wirewood Elf");
        harness.assertOnBattlefield(player2, "Glory Seeker");
    }

    @Test
    void exilesAnAuraAlongWithItsEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new EverythingComesToDust()));
        harness.addMana(player1, ManaColor.WHITE, 10);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(creature.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(aura.getCard().getId())).isNotNull();
        harness.assertNotInGraveyard(player2, "Holy Strength");
    }

    @Test
    void exilesAnArtifactCreatureEvenWhenItConvokedTheSpell() {
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new Psychomancer());
        harness.setHand(player1, List.of(new EverythingComesToDust()));
        harness.addMana(player1, ManaColor.WHITE, 9);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(convoker.getId()));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(convoker.getCard().getId())).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void aCreatureSeesArtifactsExiledInTheSameEventAsItself() {
        harness.addToBattlefield(player1, new Psychomancer());
        harness.addToBattlefield(player1, new DarksteelRelic());
        harness.setHand(player1, List.of(new EverythingComesToDust()));
        harness.addMana(player1, ManaColor.WHITE, 10);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        for (int i = 0; i < 2; i++) {
            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
            harness.handlePermanentChosen(player1, player2.getId());
        }
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }
}
