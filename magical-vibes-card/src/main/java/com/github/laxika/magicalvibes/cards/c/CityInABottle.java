package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SpellsAndLandsWithSpecifiedNamesCantBePlayedEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNameInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "VMA", collectorNumber = "265")
public class CityInABottle extends Card {

    private static final Set<String> ARABIAN_NIGHTS_CARD_NAMES = Set.of(
            "Abu Ja'far",
            "Army of Allah",
            "Camel",
            "Eye for an Eye",
            "Jihad",
            "King Suleiman",
            "Moorish Cavalry",
            "Piety",
            "Repentant Blacksmith",
            "Shahrazad",
            "War Elephant",
            "Dandân",
            "Fishliver Oil",
            "Flying Men",
            "Giant Tortoise",
            "Island Fish Jasconius",
            "Merchant Ship",
            "Old Man of the Sea",
            "Serendib Djinn",
            "Serendib Efreet",
            "Sindbad",
            "Unstable Mutation",
            "Cuombajj Witches",
            "El-Hajjâj",
            "Erg Raiders",
            "Guardian Beast",
            "Hasran Ogress",
            "Junún Efreet",
            "Juzám Djinn",
            "Khabál Ghoul",
            "Oubliette",
            "Sorceress Queen",
            "Stone-Throwing Devils",
            "Aladdin",
            "Ali Baba",
            "Ali from Cairo",
            "Bird Maiden",
            "Desert Nomads",
            "Hurr Jackal",
            "Kird Ape",
            "Magnetic Mountain",
            "Mijae Djinn",
            "Rukh Egg",
            "Ydwen Efreet",
            "Cyclone",
            "Desert Twister",
            "Drop of Honey",
            "Erhnam Djinn",
            "Ghazbán Ogre",
            "Ifh-Bíff Efreet",
            "Metamorphosis",
            "Nafs Asp",
            "Sandstorm",
            "Singing Tree",
            "Wyluli Wolf",
            "Aladdin's Lamp",
            "Aladdin's Ring",
            "Bottle of Suleiman",
            "Brass Man",
            "City in a Bottle",
            "Dancing Scimitar",
            "Ebony Horse",
            "Flying Carpet",
            "Jandor's Ring",
            "Jandor's Saddlebags",
            "Jeweled Bird",
            "Pyramids",
            "Ring of Ma'rûf",
            "Sandals of Abdallah",
            "Bazaar of Baghdad",
            "City of Brass",
            "Desert",
            "Diamond Valley",
            "Elephant Graveyard",
            "Island of Wak-Wak",
            "Library of Alexandria",
            "Mountain",
            "Oasis"
    );

    private static final PermanentPredicate OTHER_ARABIAN_NIGHTS_PERMANENT = new PermanentAllOfPredicate(List.of(
            new PermanentNameInPredicate(ARABIAN_NIGHTS_CARD_NAMES),
            new PermanentNotPredicate(new PermanentIsTokenPredicate()),
            new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
    ));

    public CityInABottle() {
        addEffect(EffectSlot.STATIC,
                new SpellsAndLandsWithSpecifiedNamesCantBePlayedEffect(ARABIAN_NIGHTS_CARD_NAMES));
        addEffect(EffectSlot.STATE_TRIGGERED, StateTriggerEffect.whenBattlefieldHasAtLeast(
                1,
                OTHER_ARABIAN_NIGHTS_PERMANENT,
                List.of(new SacrificeEachMatchingPermanentEffect(OTHER_ARABIAN_NIGHTS_PERMANENT)),
                "City in a Bottle's state-triggered ability"));
    }
}
