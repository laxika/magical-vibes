package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNameInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HML", collectorNumber = "101")
public class ApocalypseChime extends Card {

    /**
     * Every permanent-card name in the Homelands expansion. The ability keys off the <em>name</em>,
     * not the printing, so a later reprint (Serrated Arrows in a core set) is destroyed too, while a
     * Homelands instant or sorcery name can never be on the battlefield and is left out.
     */
    private static final Set<String> HOMELANDS_PERMANENT_NAMES = Set.of(
            "Abbey Gargoyles", "Abbey Matron", "Aether Storm", "Ambush Party", "Anaba Ancestor",
            "Anaba Bodyguard", "Anaba Shaman", "Anaba Spirit Crafter", "An-Havva Constable",
            "An-Havva Township", "An-Zerrin Ruins", "Apocalypse Chime", "Autumn Willow",
            "Aysen Abbey", "Aysen Bureaucrats", "Aysen Crusader", "Aysen Highway", "Baron Sengir",
            "Beast Walkers", "Black Carriage", "Carapace", "Castle Sengir", "Cemetery Gate",
            "Chandler", "Clockwork Gnomes", "Clockwork Steed", "Clockwork Swarm", "Coral Reef",
            "Dark Maze", "Daughter of Autumn", "Death Speakers", "Didgeridoo", "Drudge Spell",
            "Dwarven Pony", "Dwarven Sea Clan", "Dwarven Trader", "Ebony Rhino",
            "Eron the Relentless", "Faerie Noble", "Feast of the Unicorn", "Feroz's Ban",
            "Folk of An-Havva", "Funeral March", "Ghost Hounds", "Giant Albatross", "Giant Oyster",
            "Grandmother Sengir", "Greater Werewolf", "Hazduhr the Abbot", "Heart Wolf",
            "Hungry Mist", "Ihsan's Shade", "Irini Sengir", "Ironclaw Curse", "Joven",
            "Joven's Ferrets", "Joven's Tools", "Koskun Falls", "Koskun Keep", "Labyrinth Minotaur",
            "Leaping Lizard", "Mammoth Harness", "Marjhan", "Mesa Falcon", "Mystic Decree",
            "Narwhal", "Orcish Mine", "Primal Order", "Rashka the Slayer", "Reef Pirates",
            "Reveka, Wizard Savant", "Root Spider", "Roots", "Roterothopter", "Rysorian Badger",
            "Samite Alchemist", "Sea Sprite", "Sea Troll", "Sengir Autocrat", "Sengir Bats",
            "Serra Aviary", "Serra Bestiary", "Serra Inquisitors", "Serra Paladin",
            "Serrated Arrows", "Soraya the Falconer", "Spectral Bears", "Timmerian Fiends",
            "Torture", "Trade Caravan", "Veldrane of Sengir", "Wall of Kelp", "Willow Faerie",
            "Willow Priestess", "Wizards' School");

    public ApocalypseChime() {
        // {2}, {T}, Sacrifice this artifact: Destroy all nontoken permanents with a name originally
        // printed in the Homelands expansion. They can't be regenerated.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SacrificeSelfCost(), new DestroyAllPermanentsEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentNameInPredicate(HOMELANDS_PERMANENT_NAMES),
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()))),
                        true,
                        EachPermanentScope.ALL_PLAYERS,
                        null,
                        false)),
                "{2}, {T}, Sacrifice Apocalypse Chime: Destroy all nontoken permanents with a name "
                        + "originally printed in the Homelands expansion. They can't be regenerated."));
    }
}
