package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.a.Aethersnatch;
import com.github.laxika.magicalvibes.cards.a.AinokSurvivalist;
import com.github.laxika.magicalvibes.cards.a.AssembleThePlayers;
import com.github.laxika.magicalvibes.cards.a.AuthorityOfTheConsuls;
import com.github.laxika.magicalvibes.cards.b.BazaarOfWonders;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.Counterbore;
import com.github.laxika.magicalvibes.cards.d.DogWalker;
import com.github.laxika.magicalvibes.cards.d.DoubleMajor;
import com.github.laxika.magicalvibes.cards.d.Drought;
import com.github.laxika.magicalvibes.cards.e.ExaltedAngel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
import com.github.laxika.magicalvibes.cards.i.IceCave;
import com.github.laxika.magicalvibes.cards.i.IllusionaryMask;
import com.github.laxika.magicalvibes.cards.m.MagesContest;
import com.github.laxika.magicalvibes.cards.m.MysticSnake;
import com.github.laxika.magicalvibes.cards.r.Redirect;
import com.github.laxika.magicalvibes.cards.r.Remand;
import com.github.laxika.magicalvibes.cards.r.ReturnTheFavor;
import com.github.laxika.magicalvibes.cards.r.ReverseDamage;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Skinthinner;
import com.github.laxika.magicalvibes.cards.s.SphereOfResistance;
import com.github.laxika.magicalvibes.cards.s.Stifle;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.v.VexingShusher;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.StackEntryView;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What the server sends and logs about a face-down spell, and about an ability of a face-down permanent: only the
 * player who may look at the card learns what it is (CR 708.2, 708.4, 708.5), until it is revealed (CR 708.9).
 */
@CardUsed({Aethersnatch.class, AinokSurvivalist.class, AssembleThePlayers.class, AuthorityOfTheConsuls.class,
        BazaarOfWonders.class, Cancel.class, Counterbore.class, DogWalker.class, DoubleMajor.class, Drought.class,
        ExaltedAngel.class, GrizzlyBears.class, HermeticStudy.class, IceCave.class,
        IllusionaryMask.class, MagesContest.class, MysticSnake.class, Redirect.class,
        Remand.class, ReturnTheFavor.class, ReverseDamage.class, Shock.class, Unsummon.class,
        Skinthinner.class, SphereOfResistance.class, Stifle.class, VexingShusher.class})
class FaceDownStackIdentityTest extends BaseCardTest {

    private final ObjectMapper json = new JacksonConfig().objectMapper();

    @Test
    @DisplayName("An opponent is sent nothing that identifies a card cast with morph")
    void morphIsHiddenFromOpponent() {
        Card angel = castFaceDown(new ExaltedAngel());

        assertHiddenFrom(player2, angel);
        JsonNode view = latestStackTop(player2);
        assertThat(view.toString()).doesNotContain("Flying", "FLYING", "ANGEL", "{4}{W}{W}");
        assertThat(view.get("card").get("power").asInt()).isEqualTo(2);
        assertThat(view.get("card").get("toughness").asInt()).isEqualTo(2);
        assertThat(view.get("isSpell").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("An opponent is sent nothing that identifies a card cast with megamorph")
    void megamorphIsHiddenFromOpponent() {
        Card survivalist = castFaceDown(new AinokSurvivalist());

        assertHiddenFrom(player2, survivalist);
    }

    @Test
    @DisplayName("An opponent sees only a 2/2 with ward for a card cast with disguise")
    void disguiseIsHiddenFromOpponent() {
        Card walker = castFaceDown(new DogWalker());

        assertHiddenFrom(player2, walker);
        assertThat(latestStackTop(player2).get("card").get("keywords").toString()).isEqualTo("[\"WARD\"]");
    }

    @Test
    @DisplayName("Two different morph spells look the same to an opponent, apart from their ids")
    void differentMorphsLookTheSame() {
        Card angel = castFaceDown(new ExaltedAngel());
        String first = latestStackTop(player2).toString().replace(angel.getId().toString(), "id");
        harness.passBothPriorities();
        Card survivalist = castFaceDown(new AinokSurvivalist());
        String second = latestStackTop(player2).toString().replace(survivalist.getId().toString(), "id");

        assertThat(second).isEqualTo(first);
    }

    @Test
    @DisplayName("The controller still sees the card they cast face down")
    void controllerSeesTheirSpell() {
        castFaceDown(new ExaltedAngel());

        assertThat(latestStackTop(player1).get("card").get("name").asString()).isEqualTo("Exalted Angel");
    }

    @Test
    @DisplayName("A player who controls another player sees that player's face-down spell")
    void playerControllerSeesControlledPlayersSpell() {
        castFaceDown(new ExaltedAngel());
        gd.controlPlayer("Test", null, player2.getId(), player1.getId(), Duration.PERMANENT, false);
        harness.clearMessages();

        harness.publishState();

        assertThat(latestStackTop(player2).get("card").get("name").asString()).isEqualTo("Exalted Angel");
        assertThat(latestStackTop(player1).get("card").get("name").asString()).isEqualTo("Exalted Angel");
    }

    @Test
    @DisplayName("Controlling another player doesn't show that player the controller's face-down spell")
    void controlledPlayerDoesNotSeeControllersSpell() {
        Card angel = castFaceDown(new ExaltedAngel());
        gd.controlPlayer("Test", null, player1.getId(), player2.getId(), Duration.PERMANENT, false);

        harness.publishState();

        assertHiddenFrom(player2, angel);
    }

    @Test
    @DisplayName("A reconnect shows a face-down spell only to its controller")
    void reconnectKeepsSpellHidden() {
        Card angel = castFaceDown(new ExaltedAngel());
        GameResyncProjectionService resync =
                GameTestEngineContext.get().getBean(GameResyncProjectionService.class);

        assertHiddenIn(json.writeValueAsString(resync.currentState(gd, player2.getId())), angel);
        assertHiddenIn(json.writeValueAsString(resync.currentState(gd, null)), angel);
        assertThat(resync.currentState(gd, player1.getId()).stack())
                .extracting(StackEntryView::card).extracting(CardView::name).containsExactly("Exalted Angel");
    }

    @Test
    @DisplayName("A card cast face down from the top of a library stays hidden")
    void libraryTopCastIsHidden() {
        harness.addToBattlefield(player1, new AssembleThePlayers());
        Card walker = new DogWalker();
        markPrinting(walker);
        harness.setLibrary(player1, List.of(walker));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.ensurePriority(player1);
        harness.clearMessages();

        gs.playCardFromLibraryTop(gd, player1, null, null, List.of(), List.of(), true);

        assertHiddenFrom(player2, walker);
    }

    @Test
    @DisplayName("A creature cast face down with Illusionary Mask stays hidden")
    void illusionaryMaskCastIsHidden() {
        Card bears = offerIllusionaryMask();

        harness.handleMayAbilityChosen(player1, true);

        assertHiddenFrom(player2, bears);
    }

    @Test
    @DisplayName("A card that can't be cast face down for its cost increase isn't named")
    void failedFaceDownCastDoesNotNameCard() {
        harness.addToBattlefield(player2, new SphereOfResistance());
        Card bears = offerIllusionaryMask();
        gd.playerManaPools.get(player1.getId()).clear();
        harness.clearMessages();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(logText()).anyMatch(line -> line.contains("can't be cast face down"));
        assertHiddenFrom(player2, bears);
    }

    @Test
    @DisplayName("A face-down spell stays hidden as it resolves and enters tapped")
    void resolvingKeepsSpellHidden() {
        harness.addToBattlefield(player2, new AuthorityOfTheConsuls());
        Card angel = castFaceDown(new ExaltedAngel());
        harness.clearMessages();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Exalted Angel").isFaceDown()).isTrue();
        assertHiddenFrom(player2, angel);
    }

    @Test
    @DisplayName("A countered face-down spell is hidden until it is revealed in the graveyard")
    void counteredSpellIsRevealedOnlyInGraveyard() {
        Card angel = castFaceDown(new ExaltedAngel());
        harness.clearMessages();
        respond(new Cancel(), angel.getId(), ManaColor.BLUE);

        assertHiddenFrom(player2, angel);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(angel);
        assertThat(logText()).anyMatch(line -> line.contains("Exalted Angel is countered"));
    }

    @Test
    @DisplayName("Making a face-down spell uncounterable doesn't name it")
    void uncounterableDoesNotNameSpell() {
        harness.addToBattlefield(player2, new VexingShusher());
        Card angel = castFaceDown(new ExaltedAngel());
        harness.passPriority(player1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateAbility(player2, 0, null, angel.getId());
        harness.clearMessages();

        harness.passBothPriorities();

        assertHiddenFrom(player2, angel);
    }

    @Test
    @DisplayName("A trigger that targets a face-down spell doesn't name it")
    void targetingTriggerDoesNotNameSpell() {
        Card angel = castFaceDown(new ExaltedAngel());
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new MysticSnake()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.clearMessages();

        harness.handlePermanentChosen(player2, angel.getId());

        assertThat(gd.stack).hasSize(2);
        assertHiddenFrom(player2, angel);
    }

    @Test
    @DisplayName("A life bid to counter a face-down spell doesn't name it to the opposing bidder")
    void lifeBidDoesNotNameSpell() {
        Card angel = castFaceDown(new ExaltedAngel());
        respond(new MagesContest(), angel.getId(), ManaColor.RED);
        harness.passBothPriorities();
        harness.clearMessages();

        harness.handleXValueChosen(player1, 2);

        assertThat(harness.getConn2().getMessagesContaining("Bid life to counter")).isNotEmpty();
        assertHiddenFrom(player2, angel);
    }

    @Test
    @DisplayName("A face-down spell has no mana cost for another player to pay")
    void hiddenManaCostIsNotOffered() {
        harness.addToBattlefield(player2, new IceCave());
        harness.addMana(player2, ManaColor.WHITE, 6);
        Card angel = castFaceDown(new ExaltedAngel());
        harness.clearMessages();

        harness.passBothPriorities();

        assertThat(harness.getConn2().getMessagesContaining("{4}{W}{W}")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertHiddenFrom(player2, angel);
    }

    @Test
    @DisplayName("Choosing a face-down spell as a damage source doesn't name it")
    void chosenDamageSourceIsNotNamed() {
        Card angel = castFaceDown(new ExaltedAngel());
        respond(new ReverseDamage(), null, ManaColor.WHITE);
        harness.passBothPriorities();
        harness.clearMessages();

        harness.handlePermanentChosen(player2, angel.getId());

        assertHiddenFrom(player2, angel);
    }

    @Test
    @DisplayName("Declining new targets for a face-down spell doesn't name it in the log")
    void decliningNewTargetsDoesNotNameSpell() {
        Card angel = castFaceDown(new ExaltedAngel());
        respond(new Redirect(), angel.getId(), ManaColor.BLUE);
        harness.passBothPriorities();
        harness.clearMessages();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(logText()).anyMatch(line -> line.contains("declines to change targets for"));
        assertHiddenFrom(player2, angel);
    }

    @Test
    @DisplayName("Gaining control of a face-down spell moves the sight of it without naming it in the log")
    void gainingControlMovesSightOfSpell() {
        Card angel = castFaceDown(new ExaltedAngel());
        respond(new Aethersnatch(), angel.getId(), ManaColor.BLUE);
        harness.passBothPriorities();

        assertThat(logText()).noneMatch(line -> line.contains("Exalted Angel"));
        assertThat(latestStackTop(player2).get("card").get("name").asString()).isEqualTo("Exalted Angel");
        assertThat(latestStackTop(player1).get("card").path("name").asString("")).isEmpty();
    }

    @Test
    @DisplayName("A copy of a face-down spell is a nameless 2/2 and doesn't reveal the original")
    void copyHasOnlyFaceDownCharacteristics() {
        Card angel = castFaceDown(new ExaltedAngel());
        harness.setHand(player1, List.of(new DoubleMajor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, angel.getId());
        harness.passBothPriorities();

        StackEntry copy = gd.stack.getLast();
        assertThat(copy.isCopy()).isTrue();
        assertThat(copy.getCard().getName()).isEmpty();
        assertThat(copy.getCard().getManaCost()).isNullOrEmpty();
        assertHiddenFrom(player2, angel);

        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(token.getCard().getKeywords()).isEmpty();
        assertThat(token.getCard().getTriggeredAbilities()).isEmpty();
    }

    @Test
    @DisplayName("An ability of a face-down permanent names its source only to that permanent's controller")
    void abilityOfFaceDownPermanentIsHidden() {
        Card walker = activateAbilityOfFaceDownPermanent();

        assertHiddenFrom(player2, walker);
        assertThat(latestStackTop(player1).get("card").get("name").asString()).isEqualTo("Dog Walker");
    }

    @Test
    @DisplayName("A resolving ability of a face-down permanent isn't named in the log")
    void resolvingAbilityOfFaceDownPermanentIsHidden() {
        Card walker = activateAbilityOfFaceDownPermanent();
        harness.clearMessages();

        harness.passBothPriorities();

        assertHiddenFrom(player2, walker);
    }

    @Test
    @DisplayName("Countering an ability of a face-down permanent doesn't name its source")
    void counteredAbilityOfFaceDownPermanentIsHidden() {
        Card walker = activateAbilityOfFaceDownPermanent();
        StackEntry ability = gd.stack.getLast();
        assertThat(ability.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new Stifle()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, ability.getTargetableId());
        harness.clearMessages();

        harness.passBothPriorities();

        assertThat(gd.stack).doesNotContain(ability);
        assertHiddenFrom(player2, walker);
    }

    @Test
    @DisplayName("A keyword a face-down spell gained doesn't name it on the permanent it becomes")
    void grantedKeywordDoesNotNamePermanent() {
        Card angel = castFaceDown(new ExaltedAngel());
        gd.stack.getLast().getGrantedKeywordsOnEntry().add(Keyword.HASTE);
        harness.clearMessages();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Exalted Angel"), Keyword.HASTE)).isTrue();
        assertHiddenFrom(player2, angel);
    }

    @Test
    @DisplayName("A copy of a spell cast with disguise has ward 2")
    void copyOfDisguiseSpellHasWard() {
        Card walker = castFaceDown(new DogWalker());
        harness.setHand(player1, List.of(new DoubleMajor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, walker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.passPriority(player1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, token.getId());

        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName).contains("Shock");
    }

    @Test
    @DisplayName("A face-down permanent returned to a hand is revealed in the log")
    void leavingPermanentIsRevealed() {
        Card angel = castFaceDown(new ExaltedAngel());
        harness.passBothPriorities();
        respond(new Unsummon(), findPermanent(player1, "Exalted Angel").getId(), ManaColor.BLUE);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(angel);
        assertThat(logText()).anyMatch(line -> line.contains("Exalted Angel"));
    }

    @Test
    @DisplayName("A face-down spell returned to a hand is revealed in the log")
    void spellReturnedToHandIsRevealed() {
        Card angel = castFaceDown(new ExaltedAngel());
        respond(new Remand(), angel.getId(), ManaColor.BLUE);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(angel);
        assertThat(logText()).anyMatch(line -> line.contains("Exalted Angel"));
    }

    @Test
    @DisplayName("A cost for the mana symbols of a spell doesn't give away a face-down spell's")
    void hiddenManaSymbolsAreNotTaxed() {
        harness.addToBattlefield(player2, new Drought());

        Card skinthinner = castFaceDown(new Skinthinner());

        assertThat(gd.stack).hasSize(1);
        assertHiddenFrom(player2, skinthinner);
    }

    @Test
    @DisplayName("An effect that compares a spell's name finds none on a face-down spell")
    void nameComparisonDoesNotReadHiddenName() {
        harness.addToBattlefield(player2, new BazaarOfWonders());
        Card angel = castFaceDown(new ExaltedAngel());
        harness.clearMessages();

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertHiddenFrom(player2, angel);
    }

    @Test
    @DisplayName("A search for cards named as an uncounterable face-down spell finds no name")
    void sameNameSearchDoesNotReadHiddenName() {
        Card angel = castFaceDown(new ExaltedAngel());
        Card otherAngel = new ExaltedAngel();
        harness.setLibrary(player1, List.of(otherAngel));
        gd.spellsMadeUncounterable.add(angel.getId());
        respond(new Counterbore(), angel.getId(), ManaColor.BLUE);
        harness.clearMessages();

        harness.passBothPriorities();

        assertHiddenFrom(player2, angel);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).contains(otherAngel);
    }

    @Test
    @DisplayName("Copying an ability of a face-down permanent names its source to nobody else")
    void copiedAbilityOfFaceDownPermanentIsHidden() {
        Card walker = activateAbilityOfFaceDownPermanent();
        castReturnTheFavor(0);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertHiddenFrom(player2, walker);
    }

    @Test
    @DisplayName("Changing the target of an ability of a face-down permanent doesn't name it in the prompt")
    void retargetedAbilityOfFaceDownPermanentIsHidden() {
        Card walker = activateAbilityOfFaceDownPermanent();
        castReturnTheFavor(1);

        harness.passBothPriorities();

        assertThat(harness.getConn2().getMessagesContaining("Choose a new target for")).isNotEmpty();
        assertHiddenFrom(player2, walker);
    }

    private void castReturnTheFavor(int mode) {
        UUID abilityId = gd.stack.getLast().getTargetableId();
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new ReturnTheFavor()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{mode}, List.of(abilityId));
        harness.clearMessages();
    }

    private Card castFaceDown(Card card) {
        markPrinting(card);
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.clearMessages();
        harness.castCreatureWithMorph(player1, 0);
        return card;
    }

    private void respond(Card card, UUID targetId, ManaColor color) {
        harness.passPriority(player1);
        harness.setHand(player2, List.of(card));
        harness.addMana(player2, color, 6);
        if (targetId == null) {
            harness.castInstant(player2, 0);
        } else {
            harness.castInstant(player2, 0, targetId);
        }
    }

    private Card offerIllusionaryMask() {
        harness.addToBattlefield(player1, new IllusionaryMask());
        Card bears = new GrizzlyBears();
        markPrinting(bears);
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.clearMessages();
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        return bears;
    }

    /** Puts an ability on the stack that an Aura granted to a face-down permanent, so its source is that card. */
    private Card activateAbilityOfFaceDownPermanent() {
        Card walker = castFaceDown(new DogWalker());
        harness.passBothPriorities();
        Permanent permanent = findPermanent(player1, "Dog Walker");
        permanent.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(permanent.getId());
        harness.clearMessages();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(permanent), null,
                player2.getId());

        assertThat(gd.stack).hasSize(1);
        return walker;
    }

    /** Test cards have no printing; a client looks a card up by it, so the hidden card gets one. */
    private void markPrinting(Card card) {
        card.setSetCode("TST");
        card.setCollectorNumber("901");
    }

    private List<String> logText() {
        return gd.gameLog.stream().map(GameLogEntry::plainText).toList();
    }

    private FakeConnection connectionOf(Player player) {
        return player == player1 ? harness.getConn1() : harness.getConn2();
    }

    private JsonNode latestStackTop(Player viewer) {
        JsonNode top = null;
        for (String message : connectionOf(viewer).getSentMessages()) {
            JsonNode stack = json.readTree(message).get("stack");
            if (stack != null && !stack.isEmpty()) {
                top = stack.get(stack.size() - 1);
            }
        }
        assertThat(top).as("a message with a stack").isNotNull();
        return top;
    }

    private void assertHiddenFrom(Player viewer, Card card) {
        List<String> messages = connectionOf(viewer).getSentMessages();
        assertThat(messages).isNotEmpty();
        messages.forEach(message -> assertHiddenIn(message, card));
    }

    /** Neither the name nor the printing, which names the card just as well, appears anywhere in {@code message}. */
    private void assertHiddenIn(String message, Card card) {
        assertNoIdentity(json.readTree(message), card, "");
    }

    private void assertNoIdentity(JsonNode node, Card card, String path) {
        if (node.isString()) {
            assertThat(node.asString()).as("text sent at %s", path).doesNotContain(card.getName());
        }
        if (node.isObject() && node.hasNonNull("setCode") && node.hasNonNull("collectorNumber")) {
            assertThat(node.get("setCode").asString() + ":" + node.get("collectorNumber").asString())
                    .as("printing sent at %s", path)
                    .isNotEqualTo(card.getSetCode() + ":" + card.getCollectorNumber());
        }
        if (node.isObject()) {
            node.properties().forEach(
                    field -> assertNoIdentity(field.getValue(), card, path + "/" + field.getKey()));
        } else {
            node.forEach(child -> assertNoIdentity(child, card, path));
        }
    }
}
