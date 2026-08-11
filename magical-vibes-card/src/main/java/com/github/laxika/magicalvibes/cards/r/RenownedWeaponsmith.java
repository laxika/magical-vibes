package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "72")
public class RenownedWeaponsmith extends Card {

    public RenownedWeaponsmith() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(ManaColor.COLORLESS, 2, new ManaRestriction.ArtifactSpells())),
                "{T}: Add {C}{C}. Spend this mana only to cast artifact spells or activate abilities of artifacts."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new SearchLibraryEffect(
                        new CardAnyOfPredicate(List.<CardPredicate>of(
                                new CardNamedPredicate("Heart-Piercer Bow"),
                                new CardNamedPredicate("Vial of Dragonfire"))),
                        LibrarySearchDestination.HAND)),
                "{U}, {T}: Search your library for a card named Heart-Piercer Bow or Vial of Dragonfire, reveal it, put it into your hand, then shuffle."
        ));
    }
}
