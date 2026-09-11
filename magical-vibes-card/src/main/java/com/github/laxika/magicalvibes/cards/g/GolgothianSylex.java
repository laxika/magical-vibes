package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNameInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ATQ", collectorNumber = "51")
public class GolgothianSylex extends Card {

    /** Every card name originally printed in ATQ, including its six additional land printings. */
    private static final Set<String> ANTIQUITIES_CARD_NAMES = Set.of(
            "Argivian Archaeologist", "Argivian Blacksmith", "Artifact Ward",
            "Circle of Protection: Artifacts", "Damping Field", "Martyrs of Korlis",
            "Reverse Polarity", "Drafna's Restoration", "Energy Flux", "Hurkyl's Recall",
            "Power Artifact", "Reconstruction", "Sage of Lat-Nam", "Transmute Artifact",
            "Artifact Possession", "Gate to Phyrexia", "Haunting Wind", "Phyrexian Gremlins",
            "Priest of Yawgmoth", "Xenic Poltergeist", "Yawgmoth Demon", "Artifact Blast", "Atog",
            "Detonate", "Dwarven Weaponsmith", "Goblin Artisans", "Orcish Mechanics", "Shatterstorm",
            "Argothian Pixies", "Argothian Treefolk", "Citanul Druid", "Crumble", "Gaea's Avenger",
            "Powerleech", "Titania's Song", "Amulet of Kroog", "Armageddon Clock", "Ashnod's Altar",
            "Ashnod's Battle Gear", "Ashnod's Transmogrant", "Battering Ram", "Bronze Tablet",
            "Candelabra of Tawnos", "Clay Statue", "Clockwork Avian", "Colossus of Sardia",
            "Coral Helm", "Cursed Rack", "Dragon Engine", "Feldon's Cane", "Golgothian Sylex",
            "Grapeshot Catapult", "Ivory Tower", "Jalum Tome", "Mightstone", "Millstone",
            "Mishra's War Machine", "Obelisk of Undoing", "Onulet", "Ornithopter", "Primal Clay",
            "Rakalite", "Rocket Launcher", "Shapeshifter", "Staff of Zegon", "Su-Chi", "Tablet of Epityr",
            "Tawnos's Coffin", "Tawnos's Wand", "Tawnos's Weaponry", "Tetravus", "The Rack", "Triskelion",
            "Urza's Avenger", "Urza's Chalice", "Urza's Miter", "Wall of Spears", "Weakstone", "Yotian Soldier",
            "Mishra's Factory", "Mishra's Workshop", "Strip Mine", "Urza's Mine", "Urza's Power Plant",
            "Urza's Tower");

    public GolgothianSylex() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new SacrificeEachMatchingPermanentEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentNameInPredicate(ANTIQUITIES_CARD_NAMES),
                        new PermanentNotPredicate(new PermanentIsTokenPredicate()))))),
                "{1}, {T}: Each nontoken permanent with a name originally printed in the Antiquities "
                        + "expansion is sacrificed by its controller."));
    }
}
