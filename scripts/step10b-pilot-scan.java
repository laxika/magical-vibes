import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.HauntEffect;

import java.io.File;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * EffectSlot refactor step 10b: lists every card's registrations in the pilot slots, one line each:
 * {@code class<TAB>slot<TAB>index<TAB>effect class<TAB>sacrifice-only<TAB>haunt spell}. Input for
 * {@code step10b-pilot-triggers.py --scan}: an ON_DEATH registration that only triggers on a sacrifice becomes
 * {@code Triggers.sacrificed}, and an instant's or sorcery's haunt ability {@code Triggers.putIntoGraveyardWhileResolving}.
 *
 * <p>Run it on the card classes of the revision before step 10b (cards are built without oracle data, so a spell
 * is told by its spell ability):
 * {@code javac -d out -cp CP scripts/step10b-pilot-scan.java && java -cp out:CP PilotScan CLASS_DIR... > scan.tsv},
 * where CP holds the domain jar and every card class directory.
 */
class PilotScan {

    static final Set<EffectSlot> PILOT = EnumSet.of(EffectSlot.ON_DEATH, EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
            EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
            EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_HAND, EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_LIBRARY,
            EffectSlot.ON_SELF_MILLED);

    public static void main(String[] args) throws Exception {
        for (String dir : args) {
            Path root = Path.of(dir);
            List<Path> classes;
            try (Stream<Path> walk = Files.walk(root)) {
                classes = walk.filter(p -> p.toString().endsWith(".class") && !p.toString().contains("$")).sorted().toList();
            }
            for (Path p : classes) {
                String cn = root.relativize(p).toString().replace(File.separatorChar, '.').replace(".class", "");
                Class<?> cls;
                try { cls = Class.forName(cn); } catch (Throwable t) { continue; }
                if (!Card.class.isAssignableFrom(cls) || Modifier.isAbstract(cls.getModifiers())) continue;
                Card card;
                try { var ctor = cls.getDeclaredConstructor(); ctor.setAccessible(true); card = (Card) ctor.newInstance(); } catch (Throwable t) {
                    System.err.println("cannot build " + cn + ": " + t);
                    continue;
                }
                boolean spell = !card.getSpellEffects().isEmpty();
                for (EffectSlot slot : PILOT) {
                    List<CardEffect> effects = card.getEffects(slot);
                    for (int i = 0; i < effects.size(); i++) {
                        CardEffect e = effects.get(i);
                        System.out.println(cn + "\t" + slot + "\t" + i + "\t" + e.getClass().getSimpleName() + "\t"
                                + e.onlyTriggersOnSacrifice() + "\t" + (spell && e instanceof HauntEffect));
                    }
                }
            }
        }
    }
}
