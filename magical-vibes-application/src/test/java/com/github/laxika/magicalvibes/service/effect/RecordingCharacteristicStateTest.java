package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.layer.CharacteristicState;
import com.github.laxika.magicalvibes.service.effect.DependencyFootprint.Characteristic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The CR 613.8 dependency pruning in {@code LayerSystemService} skips a pair of trials when one
 * instance wrote nothing the other read, so it is only sound if every access to a trial state is
 * recorded. These tests pin that {@link RecordingCharacteristicState} overrides every public method
 * of {@link CharacteristicState} and records each one: a value-returning method as a read, a void
 * method as a write.
 */
class RecordingCharacteristicStateTest {

    private static final UUID PERMANENT_ID = UUID.randomUUID();

    @Test
    @DisplayName("Every public CharacteristicState method is overridden by the recording state")
    void everyPublicMethodIsOverridden() {
        List<String> missing = new ArrayList<>();
        for (Method method : publicInstanceMethods()) {
            try {
                RecordingCharacteristicState.class.getDeclaredMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException notOverridden) {
                missing.add(method.toGenericString());
            }
        }

        assertThat(missing).as("CharacteristicState methods RecordingCharacteristicState must override").isEmpty();
    }

    @Test
    @DisplayName("Every value-returning method records a read and every void method records a write")
    void everyMethodRecordsItsAccess() throws Exception {
        List<String> unrecorded = new ArrayList<>();
        for (Method method : publicInstanceMethods()) {
            DependencyFootprint footprint = new DependencyFootprint();
            RecordingCharacteristicState state = new RecordingCharacteristicState(seedState(), PERMANENT_ID, footprint);
            state.startRecording();

            method.invoke(state, arguments(method));

            boolean recorded = method.getReturnType() == void.class
                    ? footprint.mayChangeWhatIsReadBy(readsEverything())
                    : writesEverything().mayChangeWhatIsReadBy(footprint);
            if (!recorded) {
                unrecorded.add(method.toGenericString());
            }
        }

        assertThat(unrecorded).as("methods whose access the recording state does not record").isEmpty();
    }

    @Test
    @DisplayName("Nothing is recorded before recording starts")
    void nothingRecordedBeforeRecordingStarts() {
        DependencyFootprint footprint = new DependencyFootprint();
        RecordingCharacteristicState state = new RecordingCharacteristicState(seedState(), PERMANENT_ID, footprint);

        state.hasKeyword(Keyword.FLYING);
        state.addKeyword(Keyword.FLYING);

        assertThat(writesEverything().mayChangeWhatIsReadBy(footprint)).isFalse();
        assertThat(footprint.mayChangeWhatIsReadBy(readsEverything())).isFalse();
    }

    private static List<Method> publicInstanceMethods() {
        return Arrays.stream(CharacteristicState.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .filter(method -> !method.isSynthetic())
                .toList();
    }

    private static CharacteristicState seedState() {
        Card card = new Card();
        card.setName("Probe");
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        return new CharacteristicState(card, new Permanent(card));
    }

    private static DependencyFootprint writesEverything() {
        DependencyFootprint footprint = new DependencyFootprint();
        for (Characteristic characteristic : Characteristic.values()) {
            footprint.write(characteristic, PERMANENT_ID);
        }
        return footprint;
    }

    private static DependencyFootprint readsEverything() {
        DependencyFootprint footprint = new DependencyFootprint();
        for (Characteristic characteristic : Characteristic.values()) {
            footprint.read(characteristic, PERMANENT_ID);
        }
        return footprint;
    }

    private static Object[] arguments(Method method) {
        return Arrays.stream(method.getParameterTypes()).map(RecordingCharacteristicStateTest::argument).toArray();
    }

    private static Object argument(Class<?> type) {
        if (type == int.class) return 1;
        if (type == long.class) return 1L;
        if (type == boolean.class) return true;
        if (type == String.class) return "Probe";
        if (type == UUID.class) return UUID.randomUUID();
        if (type.isEnum()) return type.getEnumConstants()[0];
        if (Collection.class.isAssignableFrom(type)) return List.of();
        if (type == Predicate.class) return (Predicate<Object>) ignored -> false;
        if (type == CardEffect.class) return new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF);
        if (type == ActivatedAbility.class) {
            return new ActivatedAbility(false, "{W}",
                    List.of(new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF)), "Probe ability");
        }
        throw new IllegalArgumentException("No probe argument for " + type
                + " — add one so the new CharacteristicState method is checked");
    }
}
